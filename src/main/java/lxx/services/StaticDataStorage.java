package lxx.services;

import java.util.HashMap;
import java.util.Map;

public class StaticDataStorage {

    private final Map<String, Object> data = new HashMap<String, Object>();

    public void saveData(String id, Object data) {
        assert !this.data.containsKey(id);
        this.data.put(id, data);
    }

    public <T> T getData(String id) {
        return (T) data.get(id);
    }

    public boolean containsData(String id) {
        return data.containsKey(id);
    }

}
