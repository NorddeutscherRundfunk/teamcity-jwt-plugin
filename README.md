# jwt-plugin

Plugin to enable generation of JWT in TeamCity. 
Can be used to authenticate to GCP or AWS without the need of Vault or 
generating static SA keys.

- Enable Build Feature "JWT" on you favourite build
- Environment variables will be set:
  - JWT: JSON web token
  - JWK: JSON web key
- Configure your OIDC consumer to accept the generated JWT, validated by the JWK provided

Payload of JWT looks like this
```json
{
  "sub": "Test_Test",
  "aud": "https://localhost:8111",
  "build_type_external_id": "Test_Test",
  "nbf": 1723205270,
  "triggered_by_id": 1,
  "iss": "https://localhost:8111",
  "build_number": "76",
  "exp": 1723208870,
  "triggered_by": "admin",
  "iat": 1723205270,
  "branch": ""
}
```
