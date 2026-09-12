第一个拉动项目
git clone https://github.com/AmanITTrans/apiaquiring.git

配置 Java
set jdk 21

配置最新版 Gradle
创建付款 -POST
付款是通过POST向 URL 发送请求完成的http://localhost:8080/api/bcc/create。

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
数据格式说明
表1——结构request
标题	类型	格式	要求。	描述
幂等性键	标题	细绳	是的	此请求的唯一标识。
支付ID	身体	细绳	是的	商家的唯一标识符
数量	身体	整数8	是的	以美分为单位的付款金额（十进制）。
重定向失败网址	身体	细绳	是的	失败案例的 URL
重定向成功网址	身体	细绳	是的	成功案例的 URL
订单号	身体	Array[int6]	是的	阶数为 n 的十进制整数数组
客户端 IP 地址	身体	细绳	是的	IP地址
描述	身体	细绳	是的	付款说明
回应
响应内容request为：response。响应返回 URL 和令牌。

表2 - 结构response
标题	类型	格式	要求。	描述
URL	身体	细绳	是的	要将客户端传递给的 URL
令牌	身体	细绳	是的	此付款请求的唯一令牌
response成功后的回复示例：

{
    "URL": "https://auth.unicornlab.kz/auth/YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ==",
    "token": "YmJkSkphdm9QUVpKWWVSK3Vaa1RDdEFmNENtM01rbURhTEhPY3ZBTlRsN0JCMndTS1crampMdlJXVnhvN1YxMUk4Yk5FS0hKSHFaYWtnVjZIQitJQmpRUllDdEI2b0hrRitEWDdaamQ3bDFiWHBJSnRVNWo1VWE0SExZZkhBZHZLREVMMW1ScjVVRDlmL2VBV3VUelBHNUoyZ1RSbDJESjlTUjV5ZGNOdjUwPQ=="
}
创建 WhatsApp 消息 -POST
付款是通过POST向 URL 发送请求完成的http://localhost:8080/api/sms/send。

curl --location 'localhost:8080/api/sms/send' \
--header 'Content-Type: application/json' \
--data-raw '{
    "to": "77013457877",
    "message": "Hello, you have one notification, please check."
}'
