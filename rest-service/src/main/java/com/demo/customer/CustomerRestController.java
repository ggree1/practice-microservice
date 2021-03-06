package com.demo.customer;

import com.demo.domain.Customer;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Collection;

@RestController
@RequestMapping(value = "/api/v1/customers")
public class CustomerRestController {

    @Autowired
    private CustomerRepository customerRepository;

    @RequestMapping(method = RequestMethod.OPTIONS)
    ResponseEntity<?> options() {

        return ResponseEntity
                .ok()
                .allow(HttpMethod.GET, HttpMethod.POST,
                        HttpMethod.HEAD, HttpMethod.OPTIONS,
                        HttpMethod.PUT, HttpMethod.DELETE)
                .build();
    }

    @GetMapping
    ResponseEntity<Collection<Customer>> getCollection() {
        return ResponseEntity.ok(this.customerRepository.findAll());
    }

    @GetMapping(value = "/{id}")
    ResponseEntity<Customer> get(@PathVariable Long id) {
        return this.customerRepository.findById(id).map(ResponseEntity::ok)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @ApiOperation(value = "create customer", notes = "create a customer with firstName and lastName")
    @ApiResponses({
            @ApiResponse(code = 200, message = "200 - created"),
            @ApiResponse(code = 201, message = "201 - created")
    })
    @PostMapping
    ResponseEntity<Customer> post(@RequestBody
                                  @ApiParam(name = "customer to be created",
                                          value = "firstName, lastName - no Id needed")
                                          Customer c) {

        Customer customer = this.customerRepository.save(new Customer(c
                .getFirstName(), c.getLastName()));

        URI uri = MvcUriComponentsBuilder.fromController(getClass()).path("/{id}")
                .buildAndExpand(customer.getId()).toUri();
        return ResponseEntity.created(uri).body(customer);
    }

    @DeleteMapping(value = "/{id}")
    ResponseEntity<?> delete(@PathVariable Long id) {
        return this.customerRepository.findById(id).map(c -> {
            customerRepository.delete(c);
            return ResponseEntity.noContent().build();
        }).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    ResponseEntity<?> head(@PathVariable Long id) {
        return this.customerRepository.findById(id)
                .map(exists -> ResponseEntity.noContent().build())
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @PutMapping(value = "/{id}")
    ResponseEntity<Customer> put(@PathVariable Long id, @RequestBody Customer c) {
        return this.customerRepository
                .findById(id)
                .map(
                        existing -> {
                            Customer customer = this.customerRepository.save(new Customer(existing
                                    .getId(), c.getFirstName(), c.getLastName()));
                            URI selfLink = URI.create(ServletUriComponentsBuilder.fromCurrentRequest()
                                    .toUriString());
                            return ResponseEntity.created(selfLink).body(customer);
                        }).orElseThrow(() -> new CustomerNotFoundException(id));

    }
}

