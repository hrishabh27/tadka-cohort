package com.tadka.api.data;

import com.tadka.api.domain.delivery.DeliveryAgent;
import com.tadka.api.domain.restaurants.MenuItem;
import com.tadka.api.domain.restaurants.Restaurant;
import com.tadka.api.domain.users.User;
import com.tadka.api.domain.users.UserAddress;
import com.tadka.api.domain.valueobjects.Address;
import com.tadka.api.domain.valueobjects.GeoLocation;
import com.tadka.api.domain.valueobjects.Money;
import com.tadka.api.repositories.DeliveryAgentRepository;
import com.tadka.api.repositories.RestaurantRepository;
import com.tadka.api.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataSeeder implements CommandLineRunner {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final DeliveryAgentRepository deliveryAgentRepository;

    public DemoDataSeeder(
            RestaurantRepository restaurantRepository,
            UserRepository userRepository,
            DeliveryAgentRepository deliveryAgentRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.deliveryAgentRepository = deliveryAgentRepository;
    }

    @Override
    public void run(String... args) {
        if (restaurantRepository.count() > 0) {
            return;
        }

        // 1. Restaurants & Menus
        Restaurant biryaniBlues = new Restaurant(
                "Biryani Blues",
                new Address("100 Feet Rd, Indiranagar", "Near Metro", "Bengaluru", "560038"),
                new GeoLocation(12.9784, 77.6408),
                true
        );
        biryaniBlues.addMenuItem(new MenuItem(biryaniBlues.getId(), "Chicken Dum Biryani", "Fragrant basmati rice with spiced chicken", Money.inr(350), true));
        biryaniBlues.addMenuItem(new MenuItem(biryaniBlues.getId(), "Paneer Biryani", "Dum cooked biryani with marinated paneer", Money.inr(280), true));
        biryaniBlues.addMenuItem(new MenuItem(biryaniBlues.getId(), "Mirchi Ka Salan", "Traditional accompaniment gravy", Money.inr(60), true));
        restaurantRepository.save(biryaniBlues);

        Restaurant dosaCorner = new Restaurant(
                "Dosa Corner",
                new Address("5th Block, Koramangala", "Opposite Park", "Bengaluru", "560095"),
                new GeoLocation(12.9352, 77.6245),
                true
        );
        dosaCorner.addMenuItem(new MenuItem(dosaCorner.getId(), "Masala Dosa", "Crispy dosa filled with spiced potato masala", Money.inr(90), true));
        dosaCorner.addMenuItem(new MenuItem(dosaCorner.getId(), "Ghee Roast Dosa", "Rich crispy dosa roasted with pure desi ghee", Money.inr(130), true));
        dosaCorner.addMenuItem(new MenuItem(dosaCorner.getId(), "Filter Coffee", "Authentic South Indian degree filter coffee", Money.inr(40), true));
        restaurantRepository.save(dosaCorner);

        Restaurant chaiPoint = new Restaurant(
                "Chai Point",
                new Address("Sector 2, HSR Layout", "BDA Complex", "Bengaluru", "560102"),
                new GeoLocation(12.9116, 77.6389),
                true
        );
        chaiPoint.addMenuItem(new MenuItem(chaiPoint.getId(), "Ginger Chai", "Hot freshly brewed adrak chai", Money.inr(50), true));
        chaiPoint.addMenuItem(new MenuItem(chaiPoint.getId(), "Aloo Samosa (2 pcs)", "Crispy fried snack with potato filling", Money.inr(40), true));
        chaiPoint.addMenuItem(new MenuItem(chaiPoint.getId(), "Bun Maska", "Soft bun slathered with salted butter", Money.inr(45), true));
        restaurantRepository.save(chaiPoint);

        // 2. Demo User
        User rahul = new User("Rahul Sharma", "rahul@example.com", "+919876543210");
        rahul.addAddress(new UserAddress(rahul.getId(), "Home", new Address("Flat 402, Sunshine Apts", "Indiranagar", "Bengaluru", "560038"), true));
        userRepository.save(rahul);

        // 3. Demo Delivery Agents
        DeliveryAgent ramesh = new DeliveryAgent("Ramesh Kumar", "+919123456780", new GeoLocation(12.9716, 77.6412), true);
        DeliveryAgent suresh = new DeliveryAgent("Suresh Singh", "+919123456781", new GeoLocation(12.9341, 77.6210), true);
        deliveryAgentRepository.save(ramesh);
        deliveryAgentRepository.save(suresh);
    }
}
