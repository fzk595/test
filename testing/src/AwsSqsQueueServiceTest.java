@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    class test {

        static String USERNAME = "test-login";
        static String PASSWORD = "test-password";
        static String LOGIN_SERVER_NAME = "test-login-server";
        static String EXPECTED_ACCESS_TOKEN = "sample-access-token";
        static String LIST_REPOS_JSON = "{\"repositories\": [\"repo1\", \"repo2\"]}";
        static String REPO_NAME = "repo";

        static String REPOSITORIES_PATH = "https://%s/v2/_catalog";
        static String LOGIN_PATH = "https://%s/oauth2/token?service=%s";
        static String TAGS_PATH = "https://%s/acr/v1/%s/_tags";

        Client client = mock(Client.class);
        WebResource mockWebResource = mock(WebResource.class);
        ClientResponse mockResponse = mock(ClientResponse.class);
        WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);

        AzureContainerRegistryClient azureContainerRegistryClient = new AzureContainerRegistryClient(client);

        @Test
        void authorizeTest_whenValidCredentials_thenReturnAccessToken() {
            String host = createAuthHost(LOGIN_PATH, LOGIN_SERVER_NAME);
            String jsonResponse = createJsonResponse(EXPECTED_ACCESS_TOKEN);

            mockStatusCode(host, 200);
            when(mockResponse.getEntity(String.class)).thenReturn(jsonResponse);

            String result = azureContainerRegistryClient.authorize(USERNAME, PASSWORD, LOGIN_SERVER_NAME);

            Assertions.assertEquals(EXPECTED_ACCESS_TOKEN, result);
        }

        @Test
        void authorizeTest_whenInvalidCredentials_thenThrowException() {
            String host = createAuthHost(LOGIN_PATH, LOGIN_SERVER_NAME);
            mockStatusCodeWithReasonPhrase(host, 401, "Unauthorized");

            RepoIntegrationException exception = Assertions.assertThrows(RepoIntegrationException.class,
                    () -> azureContainerRegistryClient.authorize(USERNAME, PASSWORD, LOGIN_SERVER_NAME));
            Assertions.assertTrue(exception.getMessage().contains("Unable to authorize with Azure Container Registry"));
        }

        @Test
        void getRepositoriesTest_whenValidCredentials_thenReturnRepositoryList() {
            String host = String.format(REPOSITORIES_PATH, LOGIN_SERVER_NAME);

            mockStatusCode(host, 200);
            when(mockResponse.getEntity(String.class)).thenReturn(LIST_REPOS_JSON);

            String result = azureContainerRegistryClient.getRepositories(USERNAME, PASSWORD, LOGIN_SERVER_NAME);

            Assertions.assertEquals(LIST_REPOS_JSON, result);
        }

        @Test
        void getRepositoriesTest_whenInvalidCredentials_thenThrowException() {
            String host = String.format(REPOSITORIES_PATH, LOGIN_SERVER_NAME);
            mockStatusCodeWithReasonPhrase(host, 401, "Unauthorized");

            RepoIntegrationException exception = Assertions.assertThrows(RepoIntegrationException.class,
                    () -> azureContainerRegistryClient.getRepositories(USERNAME, PASSWORD, LOGIN_SERVER_NAME));

            Assertions.assertTrue(exception.getMessage().contains("Can't get repositories list"));
        }

        @Test
        void getTagsTest_whenValidCredentials_thenReturnTagList() {
            String host = String.format(TAGS_PATH, LOGIN_SERVER_NAME, REPO_NAME);
            String jsonResponse = "{\"tags\": [\"v1.0\", \"v1.1\"]}";

            mockStatusCode(host, 200);
            when(mockResponse.getEntity(String.class)).thenReturn(jsonResponse);

            String result = azureContainerRegistryClient.getTags(USERNAME, PASSWORD, LOGIN_SERVER_NAME, REPO_NAME);

            Assertions.assertEquals(jsonResponse, result);
        }

        @Test
        void getTagsTest_whenInvalidCredentials_thenThrowException() {
            String host = String.format(TAGS_PATH, LOGIN_SERVER_NAME, REPO_NAME);
            mockStatusCodeWithReasonPhrase(host, 401, "Unauthorized");

            RepoIntegrationException exception = Assertions.assertThrows(RepoIntegrationException.class,
                    () -> azureContainerRegistryClient.getTags(USERNAME, PASSWORD, LOGIN_SERVER_NAME, REPO_NAME));

            Assertions.assertTrue(exception.getMessage().contains("Can't get tags list for repository"));
        }

        private String createAuthHost(String authUrl, String loginServerName) {
            return String.format(authUrl, loginServerName, loginServerName);
        }

        private String createJsonResponse(String expectedAccessToken) {
            return "{\"access_token\": \"" + expectedAccessToken + "\"}";
        }

        private void mockStatusCode(String host, int expectedStatus) {
            when(client.resource(host)).thenReturn(mockWebResource);
            when(mockWebResource.header(any(), any())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.type(anyString())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.accept(anyString())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.get(ClientResponse.class)).thenReturn(mockResponse);
            when(mockResponse.getStatus()).thenReturn(expectedStatus);
        }

        private void mockStatusCodeWithReasonPhrase(String host, int expectedStatus, String reasonPhrase) {
            Response.StatusType mockStatusType = mock(Response.StatusType.class);
            when(mockStatusType.getReasonPhrase()).thenReturn(reasonPhrase);

            when(client.resource(host)).thenReturn(mockWebResource);
            when(mockWebResource.header(any(), any())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.type(anyString())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.accept(anyString())).thenReturn(mockWebResourceBuilder);
            when(mockWebResourceBuilder.get(ClientResponse.class)).thenReturn(mockResponse);
            when(mockResponse.getStatus()).thenReturn(expectedStatus);
            when(mockResponse.getStatusInfo()).thenReturn(mockStatusType);
        }
    }

