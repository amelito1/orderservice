package it.euris.orderservices.repositories;


import it.euris.orderservices.entities.OrderEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ListCrudRepository<OrderEntity, Long> {
}