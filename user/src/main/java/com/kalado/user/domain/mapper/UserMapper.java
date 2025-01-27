package com.kalado.user.domain.mapper;

import com.kalado.common.dto.AdminDto;
import com.kalado.common.dto.LocationDto;
import com.kalado.common.dto.UserDto;
import com.kalado.user.domain.model.Admin;
import com.kalado.user.domain.model.Location;
import com.kalado.user.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
  UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

  UserDto userToDto(User user);
  UserDto adminToDto(Admin user);

  User dtoToUser(UserDto userDto);
  Admin dtoToAdmin(AdminDto adminDto);

  LocationDto locationToDto(Location location);

  User dtoTouser(UserDto userDto);
}
