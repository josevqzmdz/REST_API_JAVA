package com.course.practicaljava.api.server;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.course.practicaljava.api.response.ErrorResponse;
import com.course.practicaljava.entity.Car;
import com.course.practicaljava.exception.IllegalApiParamException;
import com.course.practicaljava.repository.CarElasticRepository;
import com.course.practicaljava.service.CarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RequestMapping("/api/car/v1")
@RestController
@Tag(name = "Car API", description = "Documentation for Car API")

public class CarApi {
    @Autowired
    private CarService carService;

    @Autowired
    private CarLeasticRepository carRepository;

    private static final Logger LOG = LoggerFactory.getLogger(CarApi.class);

    @GetMapping(value = "/random", produces = MediaType.APPLICATION_JSON_VALUE)

    public Car random(){
        return carService.generateCar();
    }

    @PostMapping(value = "/echo", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Echo car", description = "Echo given car input")
    public String echo(@RequestBody @io.swagger.v3.oas.annotations.parameters.RequestBody Car car){
        LOG.info("Car is: " + car);

        return car.toString();
    }

    @GetMapping(value = "/random-collection", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Car> randomCarArray(){
        var result = new ArrayList<Car>();

        for (int i=0; i < ThreadLocalRandom.current().nextInt(1, 6); i++){
            result.add(carService.generateCar());
        }
        return result;
    }

    @GetMapping(value = "/count", produces = MediaType.TEXT_PLAIN_VALUE)
    public String countCar(){
        return "There are " + carRepository.count() + " cars";
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String createNewCar(@RequestBody Car car){
        var newCar = carRepository.save(car);
        return "Saved with ID: " + newCar.getId();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Car findCarByID(@PathVariable("id") String carId){
        return carRepository.findById(carId).orElse(null);
    }

    @PutMapping(value = "/{id}", produces = MediaType.TEXT_PLAIN_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateCar(@PathVariable("id") String carId, @RequestBody Car updatedCar){
        updatedCar.setId(carId);
        var updatedCarOnElasticsearch = carRepository.save(updatedCar);

        return "Updated car with ID: " + updatedCarOnElasticsearch.getId();
    }

    @GetMapping(value = "/find-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<Car> findCarsByBrandAndColor(
        @RequestBody Car car,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size
    ){
        var pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, "price"));
        return carRepository.findByBrandAndColor(car.getBrand(), car.getColor(), pageable).getContent();
    }

    @GetMapping(value = "/cars/{brand}/{color}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "find cars by path", description = "Find cars by branch, color, on path variable")
    @ApiResponses({
        @ApiResponse
    })
}
