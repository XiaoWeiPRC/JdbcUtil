import java.lang.reflect.Type;

import com.asniie.utils.sql.core.ObjectFactory;
import com.asniie.utils.sql.interceptors.AbstractInterceptor;
import com.asniie.utils.sql.interceptors.InterceptorChain;

public final class DbVistor extends AbstractInterceptor {

	static {
		InterceptorChain.addInterceptor(new DbVistor());
	}

	private DbVistor() {
		// TODO Auto-generated constructor stub
	}

	public static <T> T create(Class<T> clazz) {
		return ObjectFactory.create(clazz);
	}

	@Override
	public Object intercept(String[] sqls, ExecType type, Type returnType) {
		Student student = new Student();
		student.setId(12358);
		student.setName("小明");
		student.setAge(25);
		student.setChinese(100);
		student.setEnglish(115);
		student.setMath(106);
		return student;
	}
}
