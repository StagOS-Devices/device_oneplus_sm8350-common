package android.os;

import android.util.Log;
import com.oplus.annotation.OplusProperty;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class OplusSystemProperties {
    private static final String TAG = "OplusSystemProperties";
    private ArrayList<String> mOplusPropertyPersistList;
    private ArrayList<String> mOplusPropertyReadOnlyList;
    private ArrayList<String> mOplusPropertySysList;

    private OplusSystemProperties() {
        this.mOplusPropertyReadOnlyList = new ArrayList<>();
        this.mOplusPropertyPersistList = new ArrayList<>();
        this.mOplusPropertySysList = new ArrayList<>();
        initOplusSystemPropertiesList();
    }

    public static class InstanceHolder {
        static OplusSystemProperties INSTANCE = new OplusSystemProperties();

        private InstanceHolder() {
        }
    }

    public static OplusSystemProperties getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private void initOplusSystemPropertiesList() {
        try {
            Field[] fields = OplusPropertyList.class.getDeclaredFields();
            for (Field field : fields) {
                boolean isAnnotation = field.isAnnotationPresent(OplusProperty.class);
                if (isAnnotation) {
                    String propertyName = (String) field.get(null);
                    Log.d(TAG, "load prop:" + propertyName);
                    OplusProperty oplusPropertyAno = (OplusProperty) field.getDeclaredAnnotation(OplusProperty.class);
                    if (oplusPropertyAno != null)
                       this.mOplusPropertySysList.add(propertyName);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "initOplusSystemPropertiesList failed.", e);
        }
    }

}
