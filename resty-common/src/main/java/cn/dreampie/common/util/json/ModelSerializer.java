package cn.dreampie.common.util.json;

import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.entity.Record;
import cn.dreampie.common.util.Stringer;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Created by ice on 14-12-31.
 */
public enum ModelSerializer implements ObjectSerializer {
  INSTANCE;

  public static ModelSerializer instance() {
    return INSTANCE;
  }


  public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType) throws IOException {
    SerializeWriter write = serializer.getWriter();
    if (object == null) {
      write.writeNull();
      return;
    }

    String mName;
    if (object instanceof Entity) {
      if (!(object instanceof Record)) {
        Method[] methods = object.getClass().getDeclaredMethods();
        JSONField fieldAnn = null;
        for (Method m : methods) {
          fieldAnn = m.getAnnotation(JSONField.class);
          mName = m.getName();
          if ((fieldAnn == null || fieldAnn.serialize()) && mName.length() > 3 && mName.startsWith("get")
              && !hasMethod((Entity<?>) object, mName)) {
            try {
              m.invoke(object);
            } catch (Exception e) {
              throw new JSONException("Method could not invoke.", e);
            }
          }
        }
      }
      serializer.write(((Entity<?>) object).getAttrs());
    }

  }

  private boolean hasMethod(Entity<?> object, String mName) {
    Map<String, Object> attrs = object.getAttrs();
    String name = mName.replace("get", "");
    return attrs.containsKey(Stringer.firstLowerCase(name))
        || attrs.containsKey(Stringer.underlineCase(name));
  }
}
