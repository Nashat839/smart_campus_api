package com.smartcampus.api;

import com.smartcampus.api.errors.ApiExceptionMapper;
import com.smartcampus.api.errors.GenericExceptionMapper;
import com.smartcampus.api.errors.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.errors.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.errors.SensorUnavailableExceptionMapper;
import com.smartcampus.api.filters.ApiLoggingFilter;
import com.smartcampus.api.resources.DiscoveryResource;
import com.smartcampus.api.resources.SensorRoomResource;
import com.smartcampus.api.resources.SensorResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        classes.add(DiscoveryResource.class);
        classes.add(SensorRoomResource.class);
        classes.add(SensorResource.class);

        classes.add(ApiLoggingFilter.class);

        classes.add(ApiExceptionMapper.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GenericExceptionMapper.class);

        return classes;
    }
}
