package invtweaks.forge.asm.compatibility;

import org.objectweb.asm.Type;

public class MethodInfo {
    public Type methodType;
    public Type methodClass;
    public String methodName;
    public boolean isStatic = false;

    public MethodInfo(Type mType, Type mClass, String name) {
        methodType = mType;
        methodClass = mClass;
        methodName = name;
    }

    public MethodInfo(Type mType, Type mClass, String name, boolean stat) {
        methodType = mType;
        methodClass = mClass;
        methodName = name;
        isStatic = stat;
    }
}
