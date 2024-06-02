package zone.huawei.tools.springlogaid.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AidBoolean {

    True, False, Default;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonCreator
    public static AidBoolean fromValue(String value) {
        for (AidBoolean aidBoolean : AidBoolean.values()) {
            if (aidBoolean.toString().equalsIgnoreCase(value)) {
                return aidBoolean;
            }
        }
        throw new IllegalArgumentException("LOG AID :: Unexpected value '" + value + "'");
    }
}