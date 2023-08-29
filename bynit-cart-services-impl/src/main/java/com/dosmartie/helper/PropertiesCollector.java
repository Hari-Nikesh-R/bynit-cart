package com.dosmartie.helper;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class PropertiesCollector {
    @Value("${auth.client.secret.id}")
    private String authId;
}
