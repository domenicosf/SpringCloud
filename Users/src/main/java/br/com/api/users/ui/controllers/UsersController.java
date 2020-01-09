package br.com.api.users.ui.controllers;

 import br.com.api.users.service.UsersService;
 import br.com.api.users.shared.UserDto;
 import br.com.api.users.ui.model.CreateUserRequestModel;
 import br.com.api.users.ui.model.CreateUserResponseModel;
 import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;

 import org.springframework.cache.annotation.CacheConfig;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.CachePut;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
 import org.springframework.web.bind.annotation.*;

 import br.com.api.users.ui.model.UserResponseModel;

@RestController
@RequestMapping("/users")
@CacheConfig(cacheNames = "users")
public class UsersController {
	
	@Autowired
	private Environment env;
	
	@Autowired
    UsersService usersService;

	@GetMapping("/status/check")
	public String status()
	{
		return "Working on port " + env.getProperty("local.server.port") + ", with token = " + env.getProperty("token.secret");
	}
 
	@PostMapping(
			consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
			)
	@Cacheable(key = "#userDetails.email")
	public ResponseEntity<CreateUserResponseModel> createUser(@RequestBody CreateUserRequestModel userDetails)
	{
		ModelMapper modelMapper = new ModelMapper(); 
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		
		UserDto userDto = modelMapper.map(userDetails, UserDto.class);
		
		UserDto createdUser = usersService.createOrUpdateUser(userDto);
		
		CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);
		
		return ResponseEntity.status(HttpStatus.CREATED).body(returnValue);
	}

	@PutMapping(
			consumes = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE },
			produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE }
	)
	@CachePut(key = "#userDetails.email")
	public ResponseEntity<CreateUserResponseModel> updateUser(@RequestBody CreateUserRequestModel userDetails)
	{

		UserDto userDto = usersService.getUserDetailsByEmail(userDetails.getEmail());

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		//Atualizando userDto
		UserDto userDtoUpdated = modelMapper.map(userDetails, UserDto.class);
		userDtoUpdated.setId(userDto.getId());
		userDtoUpdated.setUserId(userDto.getUserId());

		UserDto createdUser = usersService.createOrUpdateUser(userDto);

		CreateUserResponseModel returnValue = modelMapper.map(createdUser, CreateUserResponseModel.class);

		return ResponseEntity.status(HttpStatus.OK).body(returnValue);
	}
	
    @GetMapping(value="/{userId}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<UserResponseModel> getUser(@PathVariable("userId") String userId) {
       
        UserDto userDto = usersService.getUserByUserId(userId); 
        UserResponseModel returnValue = new ModelMapper().map(userDto, UserResponseModel.class);
        
        return ResponseEntity.status(HttpStatus.OK).body(returnValue);
    }

	@DeleteMapping(value="/{email}", produces = { MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE })
	@CacheEvict(key = "#email")
	public ResponseEntity<Long> deleteUserByEmail(@PathVariable("email") String email) {

		Long deletedId = usersService.deleteUserByEmail(email);

		return ResponseEntity.status(HttpStatus.OK).body(deletedId);
	}
	
	
}
