package com.example.hibernatebug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.hibernatebug.Employee;

@SpringBootTest
class ApplicationTests {

	@Autowired
	private SessionFactory sessionFactory;

	@Test
	@Transactional
	void reproduceBug() {

		Session session = sessionFactory.getCurrentSession();

		Employee employee = new Employee();
		employee.setCompany("test");

		session.save(employee);

		employee.getPersonId();

		// This will fail in versions after 5.6.1 when specifying
		// hibernate.default_schema.
		//
		// Hibernate generates a faulty insert statement for global temporary table
		// containing the schema.
		//
		// In 5.6.2 and 5.6.3 the schema is specified twice:
		// "insert into testing.testing.MODULE.HT_Employee select ..."
		//
		// In 5.6.4 and 5.6.5 it is still wrong, but only specified once:
		// "insert into testing.MODULE.HT_Employee select ..."
		//
		CriteriaBuilder builder = session.getCriteriaBuilder();
		CriteriaDelete<Employee> criteriaDelete = builder.createCriteriaDelete(Employee.class);
		criteriaDelete.from(Employee.class);

		int deleted = session.createQuery(criteriaDelete).executeUpdate();

		assertEquals(1, deleted);
	}

}
