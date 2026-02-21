package it.euris.orderservices.repositories;

import it.euris.orderservices.entities.OrderedProductEntity;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderedProductRepository extends
        ListCrudRepository<OrderedProductEntity, Long> {
}
