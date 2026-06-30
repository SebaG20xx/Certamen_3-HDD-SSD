package cl.usm.sansaweigh.repositories;

import cl.usm.sansaweigh.entities.RegistroPesaje;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class RegistroPesajeRepositoryImpl implements RegistroPesajeRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public RegistroPesaje insert(RegistroPesaje registro) {
        return mongoTemplate.insert(registro);
    }

    @Override
    public RegistroPesaje update(RegistroPesaje registro) {
        return mongoTemplate.save(registro);
    }

    @Override
    public RegistroPesaje findById(String id) {
        return mongoTemplate.findById(id, RegistroPesaje.class);
    }

    @Override
    public List<RegistroPesaje> findAll() {
        return mongoTemplate.findAll(RegistroPesaje.class);
    }

    @Override
    public List<RegistroPesaje> findByFecha(LocalDateTime desde, LocalDateTime hasta) {
        Query query = new Query();
        query.addCriteria(Criteria.where("createdAt").gte(desde).lte(hasta));
        return mongoTemplate.find(query, RegistroPesaje.class);
    }
}
