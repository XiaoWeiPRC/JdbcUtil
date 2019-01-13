import com.asniie.utils.sql.annotations.param;
import com.asniie.utils.sql.annotations.query;

public interface SchoolSysApi {
	@query("SELECT * FROM student WHERE id = ${id}")
	Student queryById(@param("id") int id);
	
	@query("SELECT * FROM student WHERE name = ${teacher.books.喜欢.price}")
	Student queryStudentByTeacher(@param("teacher") Teacher teacher);
}