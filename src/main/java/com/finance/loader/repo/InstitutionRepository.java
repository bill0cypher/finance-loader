package com.finance.loader.repo;

import com.finance.loader.model.Institution;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

@Repository
public interface InstitutionRepository extends ReactiveMongoRepository<Institution, String> {
}
