package zone.huawei.tools.springlogaid.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OperatingMode {
    Global, Request, DEFAULT;

    @JsonValue
    public String getValue() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonCreator
    public static OperatingMode fromValue(String value) {
        for (OperatingMode mode : OperatingMode.values()) {
            if (mode.toString().equalsIgnoreCase(value)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("LOG AID :: Unexpected value '" + value + "'");
    }
}
