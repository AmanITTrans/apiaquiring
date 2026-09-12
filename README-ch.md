# 首次拉取项目
`git clone https://github.com/AmanITTrans/apiaquiring.git`

# 配置 Java
`set jdk 21`

# 配置最新的 Gradle

# 创建支付 - `POST`

支付通过向 URL `http://localhost:8080/api/bcc/create` 发送 `POST` 请求进行。

```jsx title="请求"
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
"description": "购买一个不错的产品"
}'
```

### 数据格式描述

---

###### 表 1 — `request` 的结构

| 标题              | 类型      | 格式       | 必需 | 描述                                  |
|------------------|-----------|------------|------|----------------------------------------|
| Idempotency-Key  | 头部      | 字符串     | 是   | 此请求的唯一标识。                    |
| paymentId        | 正文      | 字符串     | 是   | 商户的唯一标识                        |
| amount           | 正文      | int8       | 是   | 以分为单位的支付金额的十进制数。    |
| redirectFailUrl  | 正文      | 字符串     | 是   | 失败情况下的 URL                     |
| redirectSuccessUrl| Body     | String     | Yes  | 成功案例的URL                   |
| orderId          | Body      | Array[int6]| Yes  | 订单的十进制整数数组     |
| clientIpAddress  | Body      | String     | Yes  | IP地址                             |
| description      | Body      | String     | Yes  | 付款描述                 |

#### 响应

对`request`的响应是`response`。响应返回URL和令牌。

###### 表2 - `response`的结构

| 标题              | 类型      | 格式       | 必需 | 描述                                  |
|------------------|-----------|------------|------|----------------------------------------|
| URL              | Body      | String     | Yes  | 用于引导客户端的URL                  |
| token            | Body      | String     | Yes  | 此付款请求的唯一令牌   |

成功情况下的示例`response`响应：

```xml
{
"URL": "https://auth.unicornlab.kz/auth/YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ==",
"token": "YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ=="
}
```


# 创建 WhatsApp 消息 - `POST`

支付通过 `POST` 请求发送到 URL `http://localhost:8080/api/sms/send`。

```jsx title="请求"
curl --location 'localhost:8080/api/sms/send' \
--header 'Content-Type: application/json' \
--data-raw '{
"to": "77013457877",
"message": "你好，你有一条通知，请查看。"
}'
```
