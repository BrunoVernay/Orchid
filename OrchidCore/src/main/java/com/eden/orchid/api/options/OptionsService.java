package com.eden.orchid.api.options;

import com.eden.common.json.JSONElement;
import com.eden.orchid.api.OrchidService;
import com.google.inject.ImplementedBy;
import org.json.JSONObject;

import java.util.Map;

/**
 * @since v1.0.0
 * @orchidApi services
 */
@ImplementedBy(OptionsServiceImpl.class)
public interface OptionsService extends OrchidService {

    default void clearOptions() {
        getService(OptionsService.class).clearOptions();
    }

    default JSONObject getOptionsData() {
        return getService(OptionsService.class).getOptionsData();
    }

    default JSONObject loadOptions() {
        return getService(OptionsService.class).loadOptions();
    }

    default JSONElement query(String pointer) {
        return getService(OptionsService.class).query(pointer);
    }

    default Map<String, Object> getSiteData(Object data) {
        return getService(OptionsService.class).getSiteData(data);
    }

}
