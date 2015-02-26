package lakenono.db.annotation;

import java.lang.annotation.*;

@Target({ElementType.FIELD})  
@Retention(RetentionPolicy.RUNTIME) 
public @interface DBField 
{
	
	boolean serialization() default true;
	String type() default "varchar";
	
}
