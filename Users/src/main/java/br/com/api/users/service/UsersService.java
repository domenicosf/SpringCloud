package br.com.api.users.service;

import br.com.api.users.shared.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsersService extends UserDetailsService{
	UserDto createOrUpdateUser(UserDto userDetails);
	UserDto getUserDetailsByEmail(String email);
	UserDto getUserByUserId(String userId);
	Long deleteUserByEmail(String email);
}
