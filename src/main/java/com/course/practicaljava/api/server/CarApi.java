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
        @ApiResponse(responseCode = "200", description = "successful connection to the API."),
        @ApiResponse(responseCode = "400", description = "Not found, please try again")
    })
    public ResponseEntity<Object> findCarsByPath(
        @PathVariable("brand") @Parameter(description = "brand to find") String brand,
        @PathVariable("color") @Parameter(description = "color to find", example = "white") String color,
        @RequestParam(name = "page", defaultValue = "0") @Parameter(description = "Page (for pagination)") int page,
        @RequestParam(name = "size", defaultValue = "10") @Parameter(description = "how many cars in each page") int size){
            var headers = new HttpHeaders();
            headers.add(HttpHeaders.SERVER, headerValue:"Spring");
            headers.add(headerName:"X-Custom", headerValue:"Custome response header");

            if (StringUtils.isNumeric(color)){
                var errorResponse = new ErrorResponse(message:"invalid color", ZonedDateTime.now());

                return new ResponseEntity(errorResponse, headers, HttpStatus.BAD_REQUEST);
            }

            var pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, ...properties:"price"));
            var cars = carRepository.findByBrandAndColor(brand, color, pageable).getContent();
            return ResponseEntity.ok().headers(headers).body(cars);
        }//end of "hoy many cars in each page" request param
        
        @GetMapping(value = "/cars", produces = MediaType.APPLICATION_JSON_VALUE)
        public List<Car> findCarsByParam(@RequestParam(name = "brand", required = true) String brand,
            @RequestParam(name = "color", required = true) String color,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
        ){
            if (StringUtils.isNumeric(color)){
                throw new IllegalArgumentException("invalid color: " + color);
            }

            if (StringUtils.isNumeric(brand)){
                throw new IllegalApiParamException("invalid brand: "+ color);
            }

            var pageable = PageRequest.of(page, size, Sort.by(Direction.DESC, ...properties:"price"));
            return carRepository.findByBrandAndColor(brand, color, pageable). getContent();
        }// end of findCarsByParam

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleInvalidColorException(IllegalArgumentException e){
        var exceptionMessage = "Exception: " + e.getMessage();
        LOG.warn(format:"{}", exceptionMessage);

        var errorResponse = new ErrorResponse(exceptionMessage, ZonedDateTime.now());

        return new ResponseEntity<>(errorResponse, headers:null, HttpStatus.BAD_REQUEST);
    }// end of response entity

    @GetMapping(value = "/cars/date", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Car> findCarsReleasedAfter(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(name = "first_release_date") LocalDate firstReleaseDate
    ){
        return carRepository.findByFirstReleaseDateAfter(firstReleaseDate);
    }// list of findCarsReleasedAfter

    @GetMapping(value = "/cars/date_before", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Car> findCarsReleasedBefore(
        @DateTimeFormat(pattern = "yyyy-MM-dd") @RequestParam(name = "first_release_date") LocalDate firstReleaseDate
    ){
        return carRepository.customQuery(firstReleaseDate);
    }// end of findCarsReleasedBefore
}
