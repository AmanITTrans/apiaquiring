package acquiring.db.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StatusTransactions {

    IN_PROGRESS   ("init",       "Ожидание завершения платежа"),
    NEW          ("new",        "Новый платеж"),
    PENDING   ("pending",       "Ожидание завершения платежа"),
    APPROVED      ("approved",      "Платёж успешно подтверждён"),
    DECLINED      ("declined",      "Платёж отклонён"),
    PAID_AND_CREATED      ("paid and created",      "Платёж успешно подтверждён"),
    EXPIRED      ("expired",      "Платёж перенесен в архив");

    private final String externalCode;
    private final String description;


    public static StatusTransactions fromExternal(String code) {
        for (StatusTransactions st : values()) {
            if (st.externalCode.equalsIgnoreCase(code)) {
                return st;
            }
        }
        return null;
    }
}
