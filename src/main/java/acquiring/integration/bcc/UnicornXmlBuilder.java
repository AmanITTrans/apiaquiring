package acquiring.integration.bcc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Статический билдер XML-запросов для БЦК (Unicorn).
 * Без сторонних зависимостей. Все значения автоматически экранируются.
 */
public final class UnicornXmlBuilder {

    /**
     * Создание платежа: auth-acquiring (service: 1330 — 1-стадийный, 1331 — 2-стадийный).
     *
     * @param point                 терминал (point)
     * @param service               "1330" или "1331"
     * @param merchantOrderId       ваш UUID (id), по нему потом спрашиваете статус
     * @param amountTiyn            сумма в тиынах (тенге * 100)
     * @param currencyCode          код валюты ISO (398 = KZT)
     * @param notifyUrl             URL коллбэка
     * @param redirectUrl           базовый редирект
     * @param redirectSuccessUrl    редирект при успехе
     * @param redirectFailUrl       редирект при ошибке
     * @param email                 email плательщика (необязательно, но рекомендуется)
     * @param ip                    IP клиента (необязательно, но рекомендуется)
     * @param description           описание платежа (необязательно)
     * @param extraAttributes       дополнительные пары name->value для <attribute> (можно null)
     */
    public static String buildAuthAcquiring(
            String point,
            String service,
            String merchantOrderId,
            long amountTiyn,
            int currencyCode,
            String notifyUrl,
            String redirectUrl,
            String redirectSuccessUrl,
            String redirectFailUrl,
            String email,
            String ip,
            String description,
            Map<String, String> extraAttributes
    ) {
        validateNotBlank(point, "point");
        validateNotBlank(service, "service");
        validateNotBlank(merchantOrderId, "merchantOrderId");
        if (amountTiyn <= 0) throw new IllegalArgumentException("amountTiyn must be > 0");
        if (currencyCode <= 0) throw new IllegalArgumentException("currencyCode must be > 0");

        StringBuilder sb = new StringBuilder(1024);
        sb.append(xmlHeader())
                .append("<request point=\"").append(esc(point)).append("\">")
                .append("<advanced service=\"").append(esc(service)).append("\" function=\"auth-acquiring\">");

        // обязательные
        attr(sb, "id", merchantOrderId);
        attr(sb, "sum", String.valueOf(amountTiyn));
        attr(sb, "amount_currency", String.valueOf(currencyCode));

        // рекомендуемые / канальные
        if (notEmpty(notifyUrl))          attr(sb, "notify_url", notifyUrl);
        if (notEmpty(redirectUrl))        attr(sb, "redirect_url", redirectUrl);
        if (notEmpty(redirectSuccessUrl)) attr(sb, "redirect_success_url", redirectSuccessUrl);
        if (notEmpty(redirectFailUrl))    attr(sb, "redirect_fail_url", redirectFailUrl);
        if (notEmpty(email))              attr(sb, "email", email);
        if (notEmpty(ip))                 attr(sb, "ip", ip);
        if (notEmpty(description))        attr(sb, "description", description);

        // дополнительные атрибуты, если нужны
        if (extraAttributes != null && !extraAttributes.isEmpty()) {
            for (Map.Entry<String, String> e : extraAttributes.entrySet()) {
                if (notEmpty(e.getKey()) && notEmpty(e.getValue())) {
                    attr(sb, e.getKey(), e.getValue());
                }
            }
        }

        sb.append("</advanced>")
                .append("</request>");
        return sb.toString();
    }

    /**
     * Запрос статуса по вашему merchantOrderId (id).
     */
    public static String buildStatus(String point, String merchantOrderId) {
        validateNotBlank(point, "point");
        validateNotBlank(merchantOrderId, "merchantOrderId");

        return xmlHeader() +
                "<request point=\"" + esc(point) + "\">" +
                "<status id=\"" + esc(merchantOrderId) + "\"/>" +
                "</request>";
    }

    /**
     * Отмена/возврат (full/partial). Универсальный запрос "cancel".
     * Для 1-стадийного — это возврат; для 2-стадийного — отмена холда до клиринга.
     *
     * @param unicornOrderId id заказа в системе Unicorn
     * @param amountTiyn     null/<=0 для полного, >0 для частичного возврата/отмены
     */
    public static String buildCancel(String point, String unicornOrderId, Long amountTiyn) {
        validateNotBlank(point, "point");
        validateNotBlank(unicornOrderId, "unicornOrderId");

        StringBuilder sb = new StringBuilder(256);
        sb.append(xmlHeader())
                .append("<request point=\"").append(esc(point)).append("\">")
                .append("<cancel id=\"").append(esc(unicornOrderId)).append("\"");
        if (amountTiyn != null && amountTiyn > 0) {
            sb.append(" sum=\"").append(amountTiyn).append("\"");
        }
        sb.append("/>")
                .append("</request>");
        return sb.toString();
    }

    // ========== HELPERS ==========

    /** Конвертация тенге → тиыны (округление HALF_UP). */
    public static long toTiyn(BigDecimal amountKzt) {
        Objects.requireNonNull(amountKzt, "amountKzt");
        return amountKzt
                .movePointRight(2)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    public static Map<String, String> attrs(Object[][] pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (pairs != null) {
            for (Object[] p : pairs) {
                if (p != null && p.length == 2 && p[0] != null && p[1] != null) {
                    map.put(String.valueOf(p[0]), String.valueOf(p[1]));
                }
            }
        }
        return map;
    }

    private static String xmlHeader() { return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"; }

    private static void attr(StringBuilder sb, String name, String value) {
        sb.append("<attribute name=\"").append(esc(name)).append("\" value=\"").append(esc(value)).append("\"/>");
    }

    private static boolean notEmpty(String s) { return s != null && !s.isBlank(); }

    private static void validateNotBlank(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static String esc(String v) {
        String s = String.valueOf(v);
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  out.append("&amp;");  break;
                case '<':  out.append("&lt;");   break;
                case '>':  out.append("&gt;");   break;
                case '\"': out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default:   out.append(c);
            }
        }
        return out.toString();
    }
}