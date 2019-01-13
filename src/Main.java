import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.asniie.utils.sql.core.ExpReader;
import com.asniie.utils.sql.core.ValueReader;

public class Main {
	public static void main(String[] args) throws Exception {
		otner();
	}

	private static String[] peek() throws IOException {
		String exp = "${teacher}.students.${teacher.${${student.${index}}}}.name";
		ExpReader reader = new ExpReader(exp);
		String[] array = reader.peek();
		reader.close();
		return array;
	}

	private static void otner() {
		SchoolSysApi api = DbVistor.create(SchoolSysApi.class);

		api.queryById(12358);

		Teacher teacher = new Teacher();
		List<Student> students = new ArrayList<>();
		Map<String, Book> books = new HashMap<>();

		String keys[] = new String[] { "最爱", "喜欢", "看过" };

		for (int i = 0; i < 10; i++) {
			Student student = new Student();
			student.setId(12358);
			student.setName("小明_" + i);
			student.setAge(25);
			student.setChinese(100);
			student.setEnglish(115);
			student.setMath(106);

			students.add(student);

			Book book = new Book();
			book.setName("《三国演义》");
			book.setPrice(35.5);

			books.put(keys[i % 3], book);
		}

		teacher.setStudents(students);
		teacher.setBooks(books);
		teacher.setId(5);

		//System.out.println(api.queryStudentByTeacher(teacher));
		api.queryStudentByTeacher(teacher);
		// other2(teacher, students);
	}

	private static void other2(Teacher teacher, List<Student> students) {
		Object regex = parseField("teacher", teacher);
		System.out.println("exec = " + regex);

		System.err.println(parseExp("${teacher.students.${teacher.id}.name}", teacher));

		Student[] students2 = new Student[] {};

		if (Object[].class.isAssignableFrom(students2.getClass())) {
			System.err.println("数组类型");
		}
		if (List.class.isAssignableFrom(students.getClass())) {
			System.err.println("List类型");
		} else {
			System.err.println(students.getClass());
		}
	}

	private final static String REGEX = "\\$\\s*\\{(.+?)\\}(?!\\s*[.}])";

	private final static Pattern PATTERN = Pattern.compile(REGEX);

	// ${teacher.students.${index}.name}
	private static Map<String, Object> mObjectMap = new HashMap<>();

	private static String parseExp(String expression, Object object) {
		Matcher matcher = PATTERN.matcher(expression);
		ValueReader mValueReader = new ValueReader();

		System.out.println("-------------------解析开始----------------");
		System.out.println("表达式 = " + expression);
		System.out.println("来自 = " + object);

		if (matcher.find()) {
			List<String> tokens = new ArrayList<>();
			StringBuilder buffer = new StringBuilder();
			StringTokenizer tokenizer = new StringTokenizer(matcher.group(1), ".", false);
			while (tokenizer.hasMoreElements()) {
				String token = tokenizer.nextToken();
				if (token.startsWith("$")) {
					buffer.append(token);
					buffer.append(".");
				} else if (token.endsWith("}")) {
					buffer.append(token);
					tokens.add(buffer.toString());
				} else {
					tokens.add(token);
				}
			}

			String key = tokens.get(0);
			if (mObjectMap.containsKey(key)) {
				object = mObjectMap.get(key);
			} else {
				mObjectMap.put(key, object);
			}

			System.err.println(tokens);
			for (int i = 1; i < tokens.size(); i++) {
				object = mValueReader.readValue(object, parseExp(tokens.get(i), object));
			}
			return object.toString();
		}
		return expression;
	}

	private static String parseField(String className, Object object) {
		String str = "SELECT * FROM student WHERE id = ${teacher.books.最爱.name}";
		String regex = "\\$\\s?\\{\\s?(([^}]+\\s?\\.\\s?[^}]+)+)\\s?\\}";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);

		if (matcher.find()) {
			String[] attrs = matcher.group(1).split("\\.");

			if (className.equals(attrs[0])) {
				for (int i = 1; i < attrs.length; i++) {
					object = accessValue(object, attrs[i]);
				}
				return escape(String.valueOf(object));
			}
		}
		return null;
	}

	private static Object accessValue(Object object, String param) {
		try {
			return parseValue(object, param);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return object;
	}

	private static String escape(String str) {
		str = str.replace("/", "//");
		str = str.replace("'", "''");
		str = str.replace("[", "/[");
		str = str.replace("]", "/]");
		str = str.replace("%", "/%");
		str = str.replace("&", "/&");
		str = str.replace("_", "/_");
		str = str.replace("(", "/(");
		str = str.replace(")", "/)");
		return str;
	}

	private static String parseMethodName(Object object, String methodName, boolean isBool) {
		methodName = methodName.replaceAll("\\s", "");

		char ch = methodName.charAt(0);
		String set = (isBool ? "is" : "get").concat(String.valueOf(Character.toUpperCase(ch)));
		return methodName.replaceFirst(String.valueOf(ch), set);
	}

	private static Object parseValue(Object object, String param) throws Exception {
		Class<?> clazz = object.getClass();
		Object obj = null;

		if (object instanceof List) {
			Method method = ArrayList.class.getDeclaredMethod("get", int.class);
			obj = method.invoke(object, Integer.valueOf(param).intValue());
		} else if (object instanceof Map) {
			Method method = Map.class.getDeclaredMethod("get", Object.class);
			obj = method.invoke(object, param);
		} else {
			Field field = clazz.getDeclaredField(param);

			Class<?> type = field.getType();

			boolean isBool = type.equals(boolean.class) || type.equals(Boolean.class);

			Method method = clazz.getMethod(parseMethodName(object, param, isBool), new Class<?>[] {});

			obj = method.invoke(object, new Object[] {});
		}

		return obj;
	}

}
