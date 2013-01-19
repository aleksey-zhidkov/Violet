package lxx.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class TestUtils {

    static void setFinal(Object obj, Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(obj, newValue);
    }

}
