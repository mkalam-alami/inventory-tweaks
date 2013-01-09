package invtweaks.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContainerGUI {
    // Size of a chest row
    int rowSize() default 9;

    // Annotation for method to get special inventory slots
    // Signature int func()
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface RowSizeCallback {}

    // Annotation for method to get special inventory slots
    // Signature Map<ContainerSection, List<Slot>> func()
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface ContainerSectionCallback {}
}
