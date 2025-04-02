package com.tripezzy.blog_service.config;

import org.hibernate.collection.spi.PersistentBag;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(context -> {
            return !(context.getSource() instanceof PersistentBag);
        });
        return modelMapper;
    }

}
