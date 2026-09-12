# First pull project
`git clone https://github.com/AmanITTrans/apiaquiring.git`

# Create a Payment - `POST`

Payments are made using the `<payment>` async payment operation.
With this operation, the gateway will connect to a pre-determined acquirer and attempt to execute the payment.
The request is single, i.e. there can be no more than one `<payment>` element in a `<request>` element.

```jsx title="Request"
curl --location 'https://ap-gateway.learnitnow.online/external/extended-cert' \
--header 'Content-Type: application/xml' \
--data-raw '<?xml version="1.0" encoding="UTF-8"?>
<request point="4338">
  <payment service="1210"
           id="b65b23ee-9469-11f0-90c9-b282c2ad6dc9"
           sum="26500"
           account="543761******1791"
           date="2025-09-18T08:30:20+0000">
    <attribute name="amount_currency" value="USD"/>
    <attribute name="notify_url" value="https://callback.url.com/callback/777e6bae"/>
    <attribute name="redirect_url" value="https://shop.page.com/all/777e6bae"/>
    <attribute name="card_pan" value="4111111111111111"/>
    <attribute name="card_name" value="John Doe"/>
    <attribute name="card_year" value="28"/>
    <attribute name="card_month" value="04"/>
    <attribute name="card_cvv" value="123"/>
    <attribute name="ip_address" value="162.162.162.162"/>
    <attribute name="email" value="johndoe@rakhmet.com"/>
    <attribute name="phone_number" value="639201154277"/>
    <attribute name="post_code" value="2601"/>
    <attribute name="address" value="123. State street"/>
    <attribute name="city" value="Larnaca"/>
    <attribute name="country_code" value="CY"/>
  </payment>
</request>'
```

### Description of data formats

---

The data formats used in this documentation are shown in Table 1.

###### Table 1 - Data formats used


| Format       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|--------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| intN         | N-byte integer                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| numeric(N,M) | Fractional, N - number of characters, M - number of characters of the fractional part. The separator of the fractional and integer parts is a dot                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| varchar(N)   | String, not exceeding N bytes in length. All strings are transmitted in UTF-8 encoding                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| Time         | Date and time strings in the format `YYYY-MM-DDTHH:mm:ss[+\|-]hhmm`, where: <br/>1. `YYYY` is the year written in 4 digits; <br/>2. `MM` is the month with leading zero; <br/>3. `DD` is the day of the month; <br/>4. `T` is the letter 'T' (separator value between date and time);  <br/>5. `HH` is the number of hours in 24-hour format with leading zero; <br/>6. `mm` is the number of minutes with leading zero; <br/>7. `ss` is the number of seconds with leading zero; <br/>8. `[+\|-]hhmm` is the time zone relative to UTC, for example +0100 represents Central European Time (CET). |

### Structural elements of the package

---

The root element of a request to the gateway is the `<request>` element. The root element of a response is the `<response>` element. All request elements need to include the point ID assigned to the merchant as follows: `<request point="17235"></request>`. The point identifies the merchant on the gateway and is tied to the certificate and credentials provided to the merchant to enable connection to the gateway.
Requests must be sent to the gateway URL: https://ap-gateway.learnitnow.online/external/extended-cert.

###### REQUESTS
The structure of the `<payment>` element is shown in Table 2.

###### Table 2 — Structure of the `<payment>` element

| Title     | Type      | Format     | Req. | Description                                                                                                                                                                                                                                          |
|-----------|-----------|------------|------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id        | Attribute | Int8       | Yes  | Merchant’s unique operation ID. The gateway guarantees that there are no repeated operations with the same id in its database. If an operation arrives that is already in the database, the response will return the current status of the operation |
| sum       | Attribute | int4       | Yes  | Transaction amount in cents. For example, sum=50 corresponds to 50 cents                                                                                                                                                                             |
| service   | Attribute | int4       | Yes  | Static service number provided to the merchant. The service number indicates the processing channel assigned to the merchant                                                                                                                         |
| account   | Attribute | varchar100 | Yes  | Masked card number corresponding to the operation. Format should be `<first 6>******<last 4>`, e.g., 510000****1234"                                                                                                                                 |
| date      | Attribute | time       | Yes  | The date the payment was received by the merchant. Used for reconciliation                                                                                                                                                                           |
| Attribute | Element   |            | No   | Used to specify additional attributes. See below for attribute options and requirements                                                                                                                                                              |

Additional attributes are specified using the `<attribute>` element. The structure of the `<attribute>` element is shown in Table 3.

###### Table 3 - Structure of the `<attribute>` element

| Title  | Type      | Format     | Req. | Description     |
|--------|-----------|------------|------|-----------------|
| name   | Attribute | varchar50  | Yes  | Attribute name  |
| value  | Attribute | varchar100 | Yes  | Attribute value |

The possible `<attribute>` parameters for `<payment>` requests are shown in Table 4. All values must be consistent with the XML format.

###### Table 4 - Possible `<attribute>` parameters for `<payment>` requests

| Name                     | Format | Req. | Description                                                                                                                                                                                                |
|--------------------------|--------|------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| amount_currency          | String | Yes  | Currency code of the transaction in ISO 4217 3-letter format, for example USD. All currencies are accepted                                                                                                 |
| notify_url               | String | Yes  | URL where system makes GET request to notify about transaction status                                                                                                                                      |
| redirect_url             | String | Yes  | URL where system makes POST request to redirect 3ds page with result attributes                                                                                                                            |
| card_pan                 | String | Yes  | Full 16-digit payment card number                                                                                                                                                                          |
| card_name                | String | Yes  | Name on card                                                                                                                                                                                               |
| card_cvv                 | String | Yes  | Card CVV code                                                                                                                                                                                              |
| card_year                | String | Yes  | Card expiry year in 2-digit format                                                                                                                                                                         |
| card_month               | String | Yes  | Card expiry month in 2-digit format with leading zero                                                                                                                                                      |
| email                    | String | Yes  | Cardholder email address                                                                                                                                                                                   |
| phone_number             | String | Yes  | Cardholder phone number in ITU-T E123 format, for example +1 607 1234 5678                                                                                                                                 |
| address                  | String | Yes  | Cardholder address street                                                                                                                                                                                  |
| city                     | String | Yes  | Cardholder city                                                                                                                                                                                            |
| province                 | String | No   | Cardholder state or province code, for example NY                                                                                                                                                          |
| post_code                | String | No   | Cardholder postal code                                                                                                                                                                                     |
| country_code             | String | Yes  | Cardholder country code in ISO 3166-1 alpha-2 two-letter format, for example US                                                                                                                            |
| 3ds-acseci               | String | No   | Electronic Commerce Indicator (ECI) value provided by the issuer’s Access Control Server (ACS) – only for transactions authenticated through a third-party MPI                                             |
| 3ds-transaction-id       | String | No   | A unique identifier for the 3DS authentication transaction  – only for transactions authenticated through a third-party MPI. Merchants must contact tech support if they wish to process such transactions |
| 3ds-authentication-token | String | No   | The authentication token provided as part of the 3DS authentication process – only for transactions authenticated through a third-party MPI                                                                |
| website_url              | String | No   | URL of the website where the payment was requested                                                                                                                                                         |

### Examples:

```xml
<request point="17235">
<payment
   id="14546"
   sum="1000"
   service="1200"
   account="5100000******1234"
	date="2023-10-12T12:00:00+0300">
   <attribute name="amount_currency" value="USD"/>
   <attribute name="notify_url" value="https://webhook.io/webhooks/path"/>
   <attribute name="redirect_url" value="https://webpage.io/finalpage/path"/>
   <attribute name="card_pan" value="5100000000001234"/>
   <attribute name="card_name" value="John Doe"/>
   <attribute name="card_cvv" value="123"/>
   <attribute name="card_year" value="26"/>
   <attribute name="card_month" value="04"/>
   <attribute name="email" value="johndoe@email.com"/>
   <attribute name="phone_number" value="+1 607 1234 5678"/>
   <attribute name="address" value=" 1 Wall Street"/>
   <attribute name="city" value="New York"/>
   <attribute name="province" value="NY"/>
   <attribute name="post_code" value="12345"/>
   <attribute name="country_code" value="US"/>
</payment>
</request>
```

#### RESPONSES

The response to the `<payment>` element is the `<result>` element. The response is returned as soon as the operation is completed, or after the timeout period (60 seconds) has passed and the gateway has not received a response from the provider. In case of timeout, the operation returns unknown status and the merchant must query the status of the operation later using the `<status>` operation. The structure of the `<result>` element is shown in Table 5.

###### Table 5 - Structure of the `<result>` element

| Title       | Type      | Format | Description                                                                                                |
|-------------|-----------|--------|------------------------------------------------------------------------------------------------------------|
| id          | Attribute | Int8   | Merchant’s unique operation ID. This is the same ID that was provided in the original `<payment>` element |
| state       | Attribute | int2   | Payment status in the system. Possible values are 40 (in progress), 60 (successful) or 80 (failed)         |
| substate    | Attribute | int2   | Substatus of payment in the system. For online payments, this can be ignored                               |
| code        | Attribute | int2   | Payment error code. For online payments, this can be ignored                                               |
| trans       | Attribute | int2   | Gateway transaction ID. This is the unique ID that the gateway assigns to the transaction                  |
| final       | Attribute | int2   | Indicator of the finality of the transaction. Possible values are 0 (not yet final) or 1 (final)           |
| server_time | Attribute | time   | Date and time of payment receipt                                                                           |

EXAMPLE `<payment>` RESPONSE IN CASE OF SUCCESS:

```xml
<response>
<result
	id="14546"
	state="60"
	substate="0"
	code="0"
	final="1"
	trans="54197601"
	server_time="2023-10-12T12:00:20+0300">
</result>
</response>
```
EXAMPLE `<payment>` RESPONSE IN CASE OF ERROR:
```xml
<response>
<result
	id="14546"
	state="80"
	substate="0"
	code="0"
	final="1"
	trans="54197601"
	server_time="2023-10-12T12:00:20+0300">
</result>
</response>
```

EXAMPLE `<payment>` RESPONSE IN CASE OF IN PROССESS:
```xml
<response>
<result
	id="12314546"
	state="40"
	substate="0"
	code="0"
	final="0"
	trans="54197601"
	server_time="2023-10-12T12:00:20+0300">
</result>
</response>
```
