package com.educore.school.config;

import com.educore.school.model.AttendanceRecord;
import com.educore.school.model.AttendanceStatus;
import com.educore.school.model.SchoolClass;
import com.educore.school.model.Student;
import com.educore.school.model.Subject;
import com.educore.school.repository.AttendanceRepository;
import com.educore.school.repository.SchoolClassRepository;
import com.educore.school.repository.StudentRepository;
import com.educore.school.repository.SubjectRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Development-only sample data. Runs only when educore.seed.enabled=true AND the students, classes and
 * subjects collections are all empty, so it never inserts duplicates. Sample records are labelled:
 * admission numbers start with "DEMO-", teachers end with "(Sample)", subject codes start with "DEMO-".
 */
@Component
@ConditionalOnProperty(name = "educore.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final StudentRepository students;
    private final SchoolClassRepository classes;
    private final SubjectRepository subjects;
    private final AttendanceRepository attendance;

    public DataSeeder(StudentRepository students, SchoolClassRepository classes, SubjectRepository subjects,
            AttendanceRepository attendance) {
        this.students = students;
        this.classes = classes;
        this.subjects = subjects;
        this.attendance = attendance;
    }

    @Override
    public void run(String... args) {
        if (students.count() > 0 || classes.count() > 0 || subjects.count() > 0) {
            log.info("Sample data skipped: database already contains records.");
            return;
        }
        String[][] classData = {
                {"Grade 7A", "Grade 7", "Mrs. Adebayo (Sample)", "30"},
                {"Grade 8A", "Grade 8", "Mr. Okafor (Sample)", "28"},
                {"Grade 9A", "Grade 9", "Ms. Bello (Sample)", "30"},
                {"Grade 10A", "Grade 10", "Mr. Nwosu (Sample)", "25"}};
        for (String[] c : classData) {
            SchoolClass sc = new SchoolClass();
            sc.setName(c[0]);
            sc.setLevel(c[1]);
            sc.setClassTeacher(c[2]);
            sc.setCapacity(Integer.parseInt(c[3]));
            classes.save(sc);
        }

        String[] names = {"Ada Obi", "Tunde Bakare", "Chioma Eze", "Ibrahim Musa", "Zainab Yusuf", "Emeka Okoro",
                "Funke Alade", "Kelechi Nnamdi", "Halima Sani", "David Ojo", "Ngozi Umeh", "Segun Ajayi",
                "Amara Chukwu", "Bola Williams", "Yetunde Fashola", "Musa Danjuma", "Ifeoma Obiora",
                "Peter Akande", "Sade Coker", "Uche Anozie"};
        List<Student> saved = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Student s = new Student();
            s.setAdmissionNumber(String.format("DEMO-%03d", i + 1));
            s.setFullName(names[i]);
            s.setEmail(names[i].toLowerCase().replace(' ', '.') + "@sample.educore.test");
            s.setGender(i % 2 == 0 ? "Female" : "Male");
            s.setClassName(classData[i % classData.length][0]);
            s.setAge(11 + (i % classData.length) * 1 + (i % 2));
            s.setRegistrationDate(LocalDate.now().minusDays(i * 3L));
            saved.add(students.save(s));
        }

        String[][] subjectData = {
                {"Mathematics", "DEMO-MTH7", "Grade 7A"}, {"English Language", "DEMO-ENG7", "Grade 7A"},
                {"Basic Science", "DEMO-SCI8", "Grade 8A"}, {"Social Studies", "DEMO-SOC8", "Grade 8A"},
                {"Chemistry", "DEMO-CHM9", "Grade 9A"}, {"Physics", "DEMO-PHY10", "Grade 10A"}};
        String[] teachers = {"Mr. Lawal (Sample)", "Mrs. Johnson (Sample)", "Dr. Ibe (Sample)"};
        for (int i = 0; i < subjectData.length; i++) {
            Subject sub = new Subject();
            sub.setName(subjectData[i][0]);
            sub.setCode(subjectData[i][1]);
            sub.setClassName(subjectData[i][2]);
            sub.setTeacherName(teachers[i % teachers.length]);
            subjects.save(sub);
        }

        for (int day = 0; day < 7; day++) {
            LocalDate date = LocalDate.now().minusDays(day);
            for (int i = 0; i < saved.size(); i++) {
                Student s = saved.get(i);
                int roll = (i * 3 + day * 5) % 10;
                AttendanceRecord r = new AttendanceRecord();
                r.setStudentId(s.getId());
                r.setStudentName(s.getFullName());
                r.setAdmissionNumber(s.getAdmissionNumber());
                r.setClassName(s.getClassName());
                r.setDate(date);
                r.setStatus(roll == 0 ? AttendanceStatus.ABSENT
                        : roll == 1 ? AttendanceStatus.LATE : AttendanceStatus.PRESENT);
                attendance.save(r);
            }
        }
        log.info("Inserted SAMPLE data: {} classes, {} students, {} subjects, 7 days of attendance.",
                classData.length, saved.size(), subjectData.length);
    }
}
