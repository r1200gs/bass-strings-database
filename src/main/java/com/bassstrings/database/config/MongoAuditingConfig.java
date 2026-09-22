package com.bassstrings.database.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/** Powers the {@code @CreatedDate}/{@code @LastModifiedDate} fields on the domain documents. */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
