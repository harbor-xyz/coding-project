package com.harbor.calendly.respositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.harbor.calendly.models.User;

public interface UserMongoRepo extends MongoRepository<User, String> {
    public User findOneByEmail(String email);
}
