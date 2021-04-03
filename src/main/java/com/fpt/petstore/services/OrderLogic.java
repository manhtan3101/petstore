package com.fpt.petstore.services;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fpt.petstore.core.dao.DAOService;
import com.fpt.petstore.core.dao.query.SimpleFilter;
import com.fpt.petstore.core.dao.query.SqlQueryParams;
import com.fpt.petstore.core.dao.query.SqlQueryTemplate;
import com.fpt.petstore.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fpt.petstore.repository.OrderRepository;
import com.fpt.petstore.util.DateUtil;

import static com.fpt.petstore.core.dao.query.Filter.FilterType.STRING_LIKE;

/**
 * @author linuss
 */

@Component
public class OrderLogic extends DAOService {

  @Autowired
  OrderRepository repo;

  public Order saveOrder(Order order) {
    int total = order.getTotal();
    List<OrderItem> items = order.getOrderItems();
    if(total == 0 && items != null) {
    	for(OrderItem item: items) {
    	  total += item.getTotal();
      }
    	order.setTotal(total);
    }
    return repo.save(order);
  }

  public Order getOrderByCode(String code) {
    return repo.getByCode(code);
  }

  public List<Order> findAllOrders() {
    return repo.findAll();
  }

  public boolean deleteOrderById(Long id) {
    repo.deleteById(id);
    return true;
  }

  public boolean deleteOrders(List<Order> orders) {
    for (Order sel : orders) {
      deleteOrderById(sel.getId());
    }
    return true;
  }

  public List<Order> findOrdersByCustomer(Customer customer) {
    return repo.findOrdersByCustomer(customer.getUsername());
  }

  public List<Order> findOrdersByEmployee(Employee employee) {
    return repo.findOrdersByEmployee(employee.getUsername());
  }
  public List<Order> listOrderbyId(long id){
    return repo.listOrderbyId(id);
  }

  SqlQueryTemplate createOrderQuery(SqlQueryParams params) {
    String[] searchFields = {"o.code", "o.label", "c.username", "c.fullName", "e.email", "e.username", "e.fullName" };
    SqlQueryTemplate.EntityTable TABLE = new SqlQueryTemplate.EntityTable(Order.class, "o");
    SqlQueryTemplate query = new SqlQueryTemplate("petstore", "Order", "Search Orders").
      SELECT_FROM(TABLE).
      SELECT(
        new SqlQueryTemplate.Field("c.username", "customerUsername"),
        new SqlQueryTemplate.Field("c.fullName",   "customerFullName"),
        new SqlQueryTemplate.Field("e.username", "employeeUsername"),
        new SqlQueryTemplate.Field("e.fullName",   "employeeFullName")).
      JOIN(new SqlQueryTemplate.Join("LEFT JOIN", Customer.class, "c").ON("c.id = o.customerId")).
      JOIN(new SqlQueryTemplate.Join("LEFT JOIN", Employee.class, "e").ON("e.id = o.employeeId")).
      FILTER(new SimpleFilter("search", STRING_LIKE, searchFields)).
      ORDERBY(new String[] { "code" }, "code", "DESC");
    if (params != null) {
      query.mergeValue(params);
    }
    return query;
  }

  public List<Map<String, Object>> searchOrders(SqlQueryParams params) {
    SqlQueryTemplate query = createOrderQuery(params);
    return query(query).getMapRecords();
  }
}
