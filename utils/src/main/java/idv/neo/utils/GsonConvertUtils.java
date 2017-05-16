package idv.neo.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Neo on 2017/4/13.
 */

public class GsonConvertUtils {
    private static final Gson sGson = new Gson();

    //http://www.jianshu.com/p/f14a8c4a34c6
    //java.lang.ClassCastException: com.google.sGson.internal.LinkedTreeMap cannot be cast to xxx
    public static <T> List<T> stringToList(String json, Class<T> clazz) {
        if (null == json) {
            return null;
        }
        return sGson.fromJson(json, new TypeToken<T>() {
        }.getType());
    }

    //http://stackoverflow.com/questions/27253555/com-google-gson-internal-linkedtreemap-cannot-be-cast-to-my-class
    public static <T> List<T> stringToArray(String json, Class<T[]> clazz) {
        if (null == json) {
            return null;
        }
        final T[] arr = sGson.fromJson(json, clazz);
        return Arrays.asList(arr); //or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
    }

    //http://blog.csdn.net/v587ge/article/details/49086511
    public static <T> T stringToType(String json, Class<T> clazz) {
        if (null == json) {
            return null;
        }
        return sGson.fromJson(json, clazz);
    }

    //http://stackoverflow.com/questions/4841952/convert-arraylistmycustomclass-to-jsonarray
    //http://stackoverflow.com/questions/18857884/how-to-convert-arraylist-of-custom-class-to-jsonarray-in-java
    //http://stackoverflow.com/questions/37091548/convert-arraylist-with-gson-to-string
    public static <T> String ArraytoJSONString(List<T> targetList) {
        if (null == targetList) {
            return null;
        }
        return sGson.toJson(targetList);
    }

    public static <T> String typetoJSONString(T target) {
        if (null == target) {
            return null;
        }
        return sGson.toJson(target);
    }

    public static <T> JsonArray ArraytoJsonArray(List<T> targetList) {
        if (null == targetList) {
            return null;
        }
        return sGson.toJsonTree(targetList).getAsJsonArray();
    }
}
