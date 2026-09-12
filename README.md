# First pull project
`git clone https://github.com/AmanITTrans/apiaquiring.git`

# Configure java
`set jdk 21`

# Configure gradle latest gradle

# Create a Payment - `POST`

Payments are made using the `POST` request to URL `http://localhost:8080/api/bcc/create`.

```jsx title="Request"
curl --location 'localhost:8080/api/bcc/create' \
--header 'Idempotency-Key: 11232323222' \
--header 'Content-Type: application/json' \
--data-raw '{
    "paymentId": "1234",
    "amount": 100100,
    "redirectFailUrl": "https://www.redirect.url/fail",
    "redirectSuccessUrl": "https://www.redirect.url/success",
    "orderId": [123,223,543],
    "clientIpAddress": "167.167.167.167",
    "description": "Buying a nice product"
}'
```

### Description of data formats

---

###### Table 1 — Structure of the `request`

| Title            | Type      | Format     | Req. | Description                            |
|------------------|-----------|------------|------|----------------------------------------|
| Idempotency-Key  | Header    | String     | Yes  | Unique identification for this request.|
| paymentId        | Body      | String     | Yes  | Unique identifier for merchant         |
| amount           | Body      | int8       | Yes  | Decimal number of payment amount in cents.|
| redirectFailUrl  | Body      | String     | Yes  | URL for failed case                    |
| redirectSuccessUrl| Body     | String     | Yes  | URL for success case                   |
| orderId          | Body      | Array[int6]| Yes  | Array of decimal integers of order     |
| clientIpAddress  | Body      | String     | Yes  | ip address                             |
| description      | Body      | String     | Yes  | description of payment                 |

#### RESPONSES

The response to the `request` is the `response`. The response returns URL and token.

###### Table 2 - Structure of the `response`

| Title            | Type      | Format     | Req. | Description                            |
|------------------|-----------|------------|------|----------------------------------------|
| URL              | Body      | String     | Yes  | URL to pass client to                  |
| token            | Body      | String     | Yes  | Unique token of this payment request   |

EXAMPLE `response` RESPONSE IN CASE OF SUCCESS:

```xml
{
    "URL": "https://auth.unicornlab.kz/auth/YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ==",
    "token": "YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ=="
}
```


# Create a WhatsApp message - `POST`

Payments are made using the `POST` request to URL `http://localhost:8080/api/sms/send`.

```jsx title="Request"
curl --location 'localhost:8080/api/sms/send' \
--header 'Content-Type: application/json' \
--data-raw '{
    "to": "77013457877",
    "message": "Hello, you have one notification, please check."
}'
```
