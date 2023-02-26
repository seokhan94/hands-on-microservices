# Authentication

## 로컬 권한 부여 서버 (Authorization Server)

### 암호 승인 흐름을 사용해 접근 토큰 획득
 
> 클라이언트는 HTTP 기본 인증을 사용해 로그인하며, 클라리언트 ID와 시크릿(writer:secret)을 자격 증명으로 사용한다.<br> 
> 자원 소유자 자격 증명은 username 및 password 매개 변수를 사용해 전송함.

* writer 클라이언트를 위한 접근 토큰
    ```
    curl -k https://writer:secret@localhost:8443/oauth/token -d grant_type=password -d username=magnus -d password=password -s |jq .
    
    {
        "access_token": "eyJhbGciO...xj8r5KL2JLV8PYzDApkQ",
        "token_type": "bearer",
        "expires_in": 599999999,
        "scope": "product:read product:write",
        "jti": "gSJY8QBIKqgkhKi1sUPOgNYooy0"
    }
    ```
    
* reader 클라이언트를 위한 접근 토큰 
    ```
    curl -k https://reader:secret@localhost:8443/oauth/token -d grant_type=password -d username=magnus -d password=password -s | jq .
    
    {
      "access_token": "eyJhbG...Dzvz9dHaBrx1e-Q",
      "token_type": "bearer",
      "expires_in": 599999999,
      "scope": "product:read",
      "jti": "GGbOUzdeQK4XDOOqFJUBBuPAR7A"
    }
    ```


### 묵시적 승인 흐름을 사용해 접근 토큰 획득

> 웹 브라우저 기반이지만 단일 페이지 웹 애플리케이션과 같이 클라이언트 시크릿을 안전하게 보호할 수 없는 클라이언트 애플리케이션을 대상으로 한다. <br>
> 권한 부여 서버에서 인증 코드 대신 접근 토큰을 받으며, 권한 코드 승인 흐름에 비해 안정성이 낮으므로 재발급 토큰은 요청할 수 없다. <br>
> 모든 애플리케이션이 자신을 writer 클라이언트라고 주장할 수 있으며, 스코프를 요청해 사용자의 API 접근 권한을 부여받을 수 있으므로 주의해야 한다.

* writer 클라이언트를 위한 접근 토큰
```
https://localhost:8443/oauth/authorize?response_type=token&client_id=writer&redirect_uri=http://my.redirect.uri&scope=product:read+product:write&state=95372

http://my.redirect.uri/#access_token=eyJhbGciOi...4i40SAfw&token_type=bearer&state=95372&expires_in=599999999&jti=W_q1iWegKeqU-9-4_jSevl-_xM4
```

* reader 클라이언트를 위한 접근 토큰
```
https://localhost:8443/oauth/authorize?response_type=token&client_id=reader&redirect_uri=http://my.redirect.uri&scope=product:read&state=48532

http://my.redirect.uri/#access_token=eyJhbGciOiJSU...ELta8ODISQQ&token_type=bearer&state=48532&expires_in=599999999&jti=x3ad3q9bsrK76lpD-B9HYlnzHws
```


### 코드 승인 흐름을 사용해 접근 토큰 획득
> 가장 안전한 OAuth2.0 승인 흐름인 코드 승인 흐름
> 보안성이 낮은 첫 번째 단계에서는 접근 토큰과 교환할 때 사용할 인증 코드를 얻고자 웹 브라우저를 사용한다.
> 코드는 웹브라우저에서 서버 측 코드와 같은 보안 계층으로 전달되며, 보안 계층에서는 인증 코드를 접근 토큰으로 교환하려는 새 요청을 권한 부여 서버로 보낸다. 이때, 출처를 확인하기 위한 클라이언트 시크릿도 함께 보낸다.

* reader 클라이언트를 위한 접근 토큰
```
https://localhost:8443/oauth/authorize?response_type=code&client_id=reader&redirect_uri=http://my.redirect.uri&scope=product:read&state=35725

http://my.redirect.uri/?code=8YCXm4&state=35725

curl -k https://reader:secret@localhost:8443/oauth/token \
-d grant_type=authorization_code \
-d client_id=reader \
-d redirect_uri=http://my.redirect.uri \
-d code=hzgwU1 -s | jq .

{
  "access_token": "eyJhbGciOiJ...fyGYIWOcyw",
  "token_type": "bearer",
  "expires_in": 599999999,
  "scope": "product:read",
  "jti": "GZbXCqTmclRoi_szJvK7tsTtGp0"
}
```

* writer 클라이언트를 위한 접근 토큰
```
https://localhost:8443/oauth/authorize?response_type=code&client_id=writer&redirect_uri=http://my.redirect.uri&scope=product:read+product:write&state=72489

http://my.redirect.uri/?code=B4o30u&state=72489

curl -k https://writer:secret@localhost:8443/oauth/token \
-d grant_type=authorization_code \
-d client_id=writer \
-d redirect_uri=http://my.redirect.uri \
-d code=Uz6rVK -s | jq .


{
  "access_token": "eyJhbGciOiJ...fyGYIWOcyw",
  "token_type": "bearer",
  "expires_in": 599999999,
  "scope": "product:read",
  "jti": "GZbXCqTmclRoi_szJvK7tsTtGp0"
}
```

## OpenID Connect(Auth0) 공급자 

### 암호 승인 흐름을 사용해 접근 토큰 획득

* reader 클라이언트를 위한 접근 토큰
```

curl --request POST \
--url "https://${TENANT_DOMAIN_NAME}/oauth/token" \
--header "content-type:application/json" \
--data "{\"grant_type\":\"password\", \"username\":\"${USER_EMAIL}\", \"password\":\"${USER_PASSWORD}\", \"audience\":\"https://localhost:8443/product-composite\", \"scope\": \"openid email product:read\", \"client_id\": \"${CLIENT_ID}\", \"client_secret\": \"${CLIENT_SECRET}\"}" -s | jq 

curl --request POST --url https://${TENANT_DOMAIN_NAME}/oauth/token --header 'content-type: application/json' --data '{"client_id":"${CLIENT_ID}","client_secret":"${CLIENT_SECRET}","audience":"https://localhost:8443/product-composite","scope": "openid email product:write product:read","grant_type":"password", "username":"${USER_EMAIL}", "password":"${USER_PASSWORD}"}' -s | jq

{
  "access_token": "eyJhbGciOi...I9pzBjUoQv1Q6mpJQ",
  "id_token": "eyJhbGc...skkprFo3OlnBQ",
  "scope": "openid email product:read",
  "expires_in": 86400,
  "token_type": "Bearer"
}

```

* writer 클라이언트를 위한 접근 토큰
```

curl --request POST \
--url "https://${TENANT_DOMAIN_NAME}/oauth/token" \
--header "content-type:application/json" \
--data "{\"grant_type\":\"password\", \"username\":\"${USER_EMAIL}\", \"password\":\"${USER_PASSWORD}\", \"audience\":\"https://localhost:8443/product-composite\", \"scope\": \"openid email product:read product:write\", \"client_id\": \"${CLIENT_ID}\", \"client_secret\": \"${CLIENT_SECRET}\"}" -s | jq

{
  "access_token": "eyJhbGciOiJ...KQ9ZQHTkSw",
  "id_token": "eyJhbGc...FQai5TymA",
  "scope": "openid email product:read product:write",
  "expires_in": 86400,
  "token_type": "Bearer"
}

```

### 묵시적 승인 흐름을 사용해 접근 토큰 획득
* reader 클라이언트를 위한 접근 토큰
```

https://${TENANT_DOMAIN_NAME}/authorize?response_type=token&scope=openid email product:read&client_id=${CLIENT_ID}&state=98421&&nonce=jxdlsjfi0fa&redirect_uri=http://my.redirect.uri&audience=https://localhost:8443/product-composite
http://my.redirect.uri/#access_token=eyJhbG...RcioQlXgtcfdM-acfzXQ&scope=openid%20email%20product%3Aread%20product%3Awrite&expires_in=7200&token_type=Bearer&state=98421
```

* writer 클라이언트를 위한 접근 토큰
```
https://${TENANT_DOMAIN_NAME}/authorize?response_type=token&scope=openid email product:read product:write&client_id=${CLIENT_ID}&state=98421&&nonce=jxdlsjfi0fa&redirect_uri=http://my.redirect.uri&audience=https://localhost:8443/product-composite
http://my.redirect.uri/#access_token=eyJhbGciO...qy7ST4ixuLA&scope=openid%20email%20product%3Aread&expires_in=7200&token_type=Bearer&state=98421
```

### 코드 승인 흐름을 사용해 접근 토큰 획득
* reader 클라이언트를 위한 접근 토큰
```
https://${TENANT_DOMAIN_NAME}/authorize?response_type=code&scope=openid email product:read&client_id=${CLIENT_ID}&state=98421&&nonce=jxdlsjfi0fa&redirect_uri=http://my.redirect.uri&audience=https://localhost:8443/product-composite
http://my.redirect.uri/?code=9D-WrKIwD2B43AemyqnbMaI_X_nrynAjLqE8Vfr8TeDRs&state=98421

curl --request POST --url https://${TENANT_DOMAIN_NAME}/oauth/token --header 'content-type: application/json' --data '{"grant_type":"authorization_code", "client_id":"${CLIENT_ID}", "client_secret":"${CLIENT_SECRET}", "code":"${CODE}", "redirect_uri":"http://my.redirect.uri"}' -s |jq .

{
  "access_token": "eyJhbGciOi...iU5vt4ku0xJpUvpSrA",
  "id_token": "eyJh...TriFeL6YCiI21qkgsw",
  "scope": "openid email product:read",
  "expires_in": 86400,
  "token_type": "Bearer"
}
```

* writer 클라이언트를 위한 접근 토큰
```
https://${TENANT_DOMAIN_NAME}/authorize?response_type=code&scope=openid email product:read product:write&client_id=${CLIENT_ID}&state=98421&&nonce=jxdlsjfi0fa&redirect_uri=http://my.redirect.uri&audience=https://localhost:8443/product-composite
http://my.redirect.uri/?code=dYPANJ3nqD8KXkrDRkTarovAJRLuZR_9baQHMeAt8J7Ke&state=98421

curl --request POST --url https://${TENANT_DOMAIN_NAME}/oauth/token --header 'content-type: application/json' --data '{"grant_type":"authorization_code", "client_id":"${CLIENT_ID}", "client_secret":"${CLIENT_SECRET}", "code":"${CODE}", "redirect_uri":"http://my.redirect.uri"}' -s |jq .

{
  "access_token": "eyJhbGciO...HmHep3DBAwzTKVISKxew",
  "id_token": "eyJhbGc...9mHY35xa6vKnhl0Q",
  "scope": "openid email product:read product:write",
  "expires_in": 86400,
  "token_type": "Bearer"
}
```


#### 추가적인 사용자 정보를 제공하는 API를 구현하고 싶다면 Auth0의 userinfo_endpoint를 호출하면 된다.
#### OpenID 디스커버리 엔드포인트를 호출하면 엔드포인트 주소를 확인할 수 있다.

```
curl -H "Authorization: Bearer $ACCESS_TOKEN" https://${TENANT_DOMAIN_NAME}/userinfo -s | jq
{
  "sub": "auth0|63b1b...14",
  "email": "seokhan94@naver.com",
  "email_verified": true
}
```