package com.shopsphere.common.config;

import com.shopsphere.catalog.entity.Category;
import com.shopsphere.catalog.entity.Product;
import com.shopsphere.catalog.entity.ProductImage;
import com.shopsphere.catalog.entity.ProductVariant;
import com.shopsphere.catalog.repository.CategoryRepository;
import com.shopsphere.catalog.repository.ProductRepository;
import com.shopsphere.user.entity.Address;
import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.repository.AddressRepository;
import com.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        initUsers();
        initCatalog();
    }

    private void initUsers() {
        if (!userRepository.existsByEmail("admin@shopsphere.com")) {
            User admin = User.builder()
                    .email("admin@shopsphere.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .firstName("Platform")
                    .lastName("Admin")
                    .phone("+1234567890")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default admin user: admin@shopsphere.com / Admin@123");
        }

        if (!userRepository.existsByEmail("customer@shopsphere.com")) {
            User customer = User.builder()
                    .email("customer@shopsphere.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .firstName("Alex")
                    .lastName("Mercer")
                    .phone("+1987654321")
                    .role(Role.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            User savedCustomer = userRepository.save(customer);

            // Add sample addresses for quick checkout testing
            Address homeAddress = Address.builder()
                    .user(savedCustomer)
                    .fullName("Alex Mercer")
                    .phone("+1987654321")
                    .streetLine1("742 Evergreen Terrace")
                    .city("Springfield")
                    .state("OR")
                    .postalCode("97477")
                    .country("United States")
                    .isDefault(true)
                    .build();

            Address officeAddress = Address.builder()
                    .user(savedCustomer)
                    .fullName("Alex Mercer (Work)")
                    .phone("+1987654322")
                    .streetLine1("100 Innovation Way")
                    .streetLine2("Suite 400")
                    .city("Portland")
                    .state("OR")
                    .postalCode("97201")
                    .country("United States")
                    .isDefault(false)
                    .build();

            addressRepository.saveAll(List.of(homeAddress, officeAddress));
            log.info("Initialized default customer user and sample shipping addresses: customer@shopsphere.com / Password@123");
        }
    }

    private void initCatalog() {
        if (categoryRepository.count() > 0) {
            return;
        }

        log.info("Seeding rich initial catalog categories and products...");

        // 1. Root Categories
        Category electronics = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .description("Cutting-edge audio, smart wearables, monitors and personal tech")
                .imageUrl("https://images.unsplash.com/photo-1498049794561-7780e7231661?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        Category fashion = Category.builder()
                .name("Apparel & Fashion")
                .slug("fashion")
                .description("Sustainably crafted clothing, designer knitwear, and modern essentials")
                .imageUrl("https://images.unsplash.com/photo-1445205170230-053b83016050?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        Category homeLiving = Category.builder()
                .name("Home & Living")
                .slug("home-living")
                .description("Artisan kitchenware, Scandinavian ceramics, and ambient home decor")
                .imageUrl("https://images.unsplash.com/photo-1513694203232-719a280e022f?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        Category fitness = Category.builder()
                .name("Fitness & Wellness")
                .slug("fitness")
                .description("High-performance yoga mats, insulated bottles, and training gear")
                .imageUrl("https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        categoryRepository.saveAll(List.of(electronics, fashion, homeLiving, fitness));

        // Subcategories
        Category audio = Category.builder()
                .name("Audio & Headphones")
                .slug("audio-headphones")
                .description("Noise-canceling headphones, studio monitors and Hi-Fi speakers")
                .parent(electronics)
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        Category computing = Category.builder()
                .name("Computers & Workspace")
                .slug("computers-workspace")
                .description("Mechanical keyboards, 4K displays and ergonomic office gear")
                .parent(electronics)
                .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80")
                .active(true)
                .build();

        categoryRepository.saveAll(List.of(audio, computing));

        // ---------------- PRODUCTS ----------------

        // 1. Headphones (Audio)
        Product headphones = Product.builder()
                .category(audio)
                .name("AeroSound Pro Noise-Canceling Headphones")
                .slug("aerosound-pro-noise-canceling-headphones")
                .description("Engineered for pure acoustic precision. The AeroSound Pro combines hybrid active noise cancellation, custom 40mm titanium drivers, 45-hour ultra-long battery life, and plush memory foam earcups.")
                .basePrice(new BigDecimal("199.99"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.85"))
                .reviewCount(42)
                .build();
        headphones.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        headphones.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&auto=format&fit=crop&q=80")
                .primary(false).displayOrder(1).build());
        headphones.addVariant(ProductVariant.builder()
                .sku("AERO-BLK").title("Midnight Black").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(28).active(true).build());
        headphones.addVariant(ProductVariant.builder()
                .sku("AERO-SLV").title("Lunar Silver").colorAttr("Silver").priceAdjustment(new BigDecimal("15.00")).stockQuantity(14).active(true).build());

        // 2. Keyboard (Computing)
        Product keyboard = Product.builder()
                .category(computing)
                .name("ErgoKey Low-Profile Mechanical Keyboard")
                .slug("ergokey-low-profile-mechanical-keyboard")
                .description("Crafted with aircraft-grade aluminum, hot-swappable mechanical switches, PBT shine-through keycaps, and multi-device Bluetooth 5.2 connectivity. The ultimate centerpiece for modern minimalist desk setups.")
                .basePrice(new BigDecimal("119.50"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.72"))
                .reviewCount(35)
                .build();
        keyboard.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        keyboard.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1595225476474-87563907a212?w=800&auto=format&fit=crop&q=80")
                .primary(false).displayOrder(1).build());
        keyboard.addVariant(ProductVariant.builder()
                .sku("ERGO-RED").title("Linear Red Switches").colorAttr("Space Gray").priceAdjustment(BigDecimal.ZERO).stockQuantity(22).active(true).build());
        keyboard.addVariant(ProductVariant.builder()
                .sku("ERGO-BRN").title("Tactile Brown Switches").colorAttr("Space Gray").priceAdjustment(new BigDecimal("5.00")).stockQuantity(16).active(true).build());

        // 3. UltraView Monitor (Computing)
        Product monitor = Product.builder()
                .category(computing)
                .name("UltraView 27\" 4K UHD IPS Designer Display")
                .slug("ultraview-27-4k-uhd-ips-designer-display")
                .description("99% DCI-P3 color accuracy, factory calibrated Delta E < 2, HDR400, and 90W USB-C single cable connectivity for charging and video output. Built for digital artists, developers, and photographers.")
                .basePrice(new BigDecimal("349.00"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.90"))
                .reviewCount(18)
                .build();
        monitor.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        monitor.addVariant(ProductVariant.builder()
                .sku("UV27-STD").title("Standard Height-Adjustable Stand").colorAttr("Silver").priceAdjustment(BigDecimal.ZERO).stockQuantity(12).active(true).build());
        monitor.addVariant(ProductVariant.builder()
                .sku("UV27-ARM").title("Desk Clamp Articulated Arm").colorAttr("Black").priceAdjustment(new BigDecimal("45.00")).stockQuantity(8).active(true).build());

        // 4. Smart Fitness Watch (Electronics)
        Product smartWatch = Product.builder()
                .category(electronics)
                .name("PulseBeat S5 Bio-Metric Smartwatch")
                .slug("pulsebeat-s5-bio-metric-smartwatch")
                .description("Continuous SpO2, sleep cycle staging, optical ECG tracking, 5ATM water resistance, and crisp Always-On AMOLED sapphire crystal display with 10-day battery life.")
                .basePrice(new BigDecimal("159.99"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.68"))
                .reviewCount(57)
                .build();
        smartWatch.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        smartWatch.addVariant(ProductVariant.builder()
                .sku("PULSE-BLK").title("Onyx Black / Fluoroelastomer").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(30).active(true).build());
        smartWatch.addVariant(ProductVariant.builder()
                .sku("PULSE-GLD").title("Rose Gold / Milanese Mesh").colorAttr("Gold").priceAdjustment(new BigDecimal("20.00")).stockQuantity(15).active(true).build());

        // 5. Organic Heavyweight Tee (Fashion)
        Product tshirt = Product.builder()
                .category(fashion)
                .name("EcoWear 100% Organic Heavyweight Tee")
                .slug("ecowear-100-organic-heavyweight-tee")
                .description("Crafted in Portugal from GOTS-certified 240 GSM long-staple combed cotton. Garment-dyed for a broken-in softness with reinforced collar tape that never sags.")
                .basePrice(new BigDecimal("29.99"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.65"))
                .reviewCount(84)
                .build();
        tshirt.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        tshirt.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1581655353564-df123a1eb820?w=800&auto=format&fit=crop&q=80")
                .primary(false).displayOrder(1).build());
        tshirt.addVariant(ProductVariant.builder()
                .sku("TEE-BLK-M").title("Medium / Onyx Black").sizeAttr("M").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(40).active(true).build());
        tshirt.addVariant(ProductVariant.builder()
                .sku("TEE-BLK-L").title("Large / Onyx Black").sizeAttr("L").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(35).active(true).build());
        tshirt.addVariant(ProductVariant.builder()
                .sku("TEE-WHT-M").title("Medium / Chalk White").sizeAttr("M").colorAttr("White").priceAdjustment(BigDecimal.ZERO).stockQuantity(25).active(true).build());
        tshirt.addVariant(ProductVariant.builder()
                .sku("TEE-WHT-L").title("Large / Chalk White").sizeAttr("L").colorAttr("White").priceAdjustment(BigDecimal.ZERO).stockQuantity(20).active(true).build());

        // 6. Commuter Backpack (Fashion)
        Product backpack = Product.builder()
                .category(fashion)
                .name("Urban Voyager Weatherproof Commuter Pack")
                .slug("urban-voyager-weatherproof-commuter-pack")
                .description("Cordura ballistic nylon shell with waterproof YKK AquaGuard zippers, padded 16-inch laptop compartment, hidden passport security pocket, and magnetic Fidlock buckles.")
                .basePrice(new BigDecimal("89.00"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.88"))
                .reviewCount(62)
                .build();
        backpack.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        backpack.addVariant(ProductVariant.builder()
                .sku("PACK-BLK-22L").title("22L Standard / Obsidian Black").sizeAttr("22L").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(20).active(true).build());
        backpack.addVariant(ProductVariant.builder()
                .sku("PACK-GRN-26L").title("26L Expanded / Alpine Olive").sizeAttr("26L").colorAttr("Olive").priceAdjustment(new BigDecimal("15.00")).stockQuantity(12).active(true).build());

        // 7. Wool Knit Sweater (Fashion)
        Product sweater = Product.builder()
                .category(fashion)
                .name("Artisan Merino Ribbed Crewneck Sweater")
                .slug("artisan-merino-ribbed-crewneck-sweater")
                .description("Ultra-fine 19.5 micron Australian Merino wool offering natural temperature regulation, odor resistance, and unmatched warmth without itchiness.")
                .basePrice(new BigDecimal("110.00"))
                .active(true)
                .featured(false)
                .ratingAvg(new BigDecimal("4.75"))
                .reviewCount(23)
                .build();
        sweater.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        sweater.addVariant(ProductVariant.builder()
                .sku("SWTR-OAT-M").title("Medium / Warm Oatmeal").sizeAttr("M").colorAttr("Beige").priceAdjustment(BigDecimal.ZERO).stockQuantity(15).active(true).build());
        sweater.addVariant(ProductVariant.builder()
                .sku("SWTR-OAT-L").title("Large / Warm Oatmeal").sizeAttr("L").colorAttr("Beige").priceAdjustment(BigDecimal.ZERO).stockQuantity(10).active(true).build());

        // 8. Ceramic Pour-Over (Home & Living)
        Product coffeeDripper = Product.builder()
                .category(homeLiving)
                .name("Kyoto Ceramic Pour-Over Coffee Station")
                .slug("kyoto-ceramic-pour-over-coffee-station")
                .description("Handcrafted matte ceramic brewer with internal spiral ribs for optimized flow rate and maximum extraction. Includes heat-resistant borosilicate glass carafe.")
                .basePrice(new BigDecimal("45.00"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.92"))
                .reviewCount(47)
                .build();
        coffeeDripper.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        coffeeDripper.addVariant(ProductVariant.builder()
                .sku("KYOTO-WHT").title("600ml / Chalk White Ceramic").sizeAttr("600ml").colorAttr("White").priceAdjustment(BigDecimal.ZERO).stockQuantity(18).active(true).build());
        coffeeDripper.addVariant(ProductVariant.builder()
                .sku("KYOTO-GRY").title("800ml / Charcoal Gray Ceramic").sizeAttr("800ml").colorAttr("Gray").priceAdjustment(new BigDecimal("8.00")).stockQuantity(14).active(true).build());

        // 9. Cast Iron Dutch Oven (Home & Living)
        Product dutchOven = Product.builder()
                .category(homeLiving)
                .name("Heritage Enameled Cast Iron Dutch Oven (5.5 Qt)")
                .slug("heritage-enameled-cast-iron-dutch-oven-5-5-qt")
                .description("Superior heat retention and even heat distribution for braising, baking artisan sourdough, roasting, and slow stews. Oven safe up to 500°F (260°C).")
                .basePrice(new BigDecimal("78.50"))
                .active(true)
                .featured(false)
                .ratingAvg(new BigDecimal("4.80"))
                .reviewCount(31)
                .build();
        dutchOven.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        dutchOven.addVariant(ProductVariant.builder()
                .sku("DO-EMERALD").title("Emerald Green Enamel").colorAttr("Green").priceAdjustment(BigDecimal.ZERO).stockQuantity(15).active(true).build());
        dutchOven.addVariant(ProductVariant.builder()
                .sku("DO-BLUE").title("Deep Provence Blue").colorAttr("Blue").priceAdjustment(BigDecimal.ZERO).stockQuantity(10).active(true).build());

        // 10. Natural Rubber Yoga Mat (Fitness)
        Product yogaMat = Product.builder()
                .category(fitness)
                .name("ZenFlow Dual-Grip Natural Tree Rubber Yoga Mat")
                .slug("zenflow-dual-grip-natural-tree-rubber-yoga-mat")
                .description("Biodegradable natural tree rubber with an ultra-absorbent polyurethane top coat that offers unparalleled traction even during high-heat sweaty vinyasa sessions.")
                .basePrice(new BigDecimal("64.00"))
                .active(true)
                .featured(true)
                .ratingAvg(new BigDecimal("4.84"))
                .reviewCount(53)
                .build();
        yogaMat.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1592432678016-e910b452f9a2?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        yogaMat.addVariant(ProductVariant.builder()
                .sku("YOGA-SAGE").title("Sage Green / 4.5mm Cushion").colorAttr("Sage").priceAdjustment(BigDecimal.ZERO).stockQuantity(25).active(true).build());
        yogaMat.addVariant(ProductVariant.builder()
                .sku("YOGA-SLATE").title("Midnight Slate / 4.5mm Cushion").colorAttr("Slate").priceAdjustment(BigDecimal.ZERO).stockQuantity(20).active(true).build());

        // 11. Insulated Water Bottle (Fitness)
        Product bottle = Product.builder()
                .category(fitness)
                .name("HydroPeak 32oz Vacuum-Insulated Flask")
                .slug("hydropeak-32oz-vacuum-insulated-flask")
                .description("Pro-grade 18/8 stainless steel, double-wall vacuum insulation keeps liquids cold for 24 hours or piping hot for 12 hours. Sweat-free powder coat exterior.")
                .basePrice(new BigDecimal("26.99"))
                .active(true)
                .featured(false)
                .ratingAvg(new BigDecimal("4.89"))
                .reviewCount(96)
                .build();
        bottle.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        bottle.addVariant(ProductVariant.builder()
                .sku("FLASK-WHT-32").title("32oz / Arctic White").sizeAttr("32oz").colorAttr("White").priceAdjustment(BigDecimal.ZERO).stockQuantity(50).active(true).build());
        bottle.addVariant(ProductVariant.builder()
                .sku("FLASK-NVY-32").title("32oz / Pacific Navy").sizeAttr("32oz").colorAttr("Navy").priceAdjustment(BigDecimal.ZERO).stockQuantity(45).active(true).build());
        bottle.addVariant(ProductVariant.builder()
                .sku("FLASK-BLK-40").title("40oz / Onyx Black").sizeAttr("40oz").colorAttr("Black").priceAdjustment(new BigDecimal("5.00")).stockQuantity(30).active(true).build());

        // 12. Minimalist Ceramic Mug (Home & Living)
        Product mug = Product.builder()
                .category(homeLiving)
                .name("Nordic Stoneware Coffee Mug (12oz)")
                .slug("nordic-stoneware-coffee-mug-12oz")
                .description("Individually wheel-thrown artisanal stoneware with a satin-matte glaze. Dishwasher and microwave safe, designed to sit comfortably in both hands.")
                .basePrice(new BigDecimal("18.50"))
                .active(true)
                .featured(false)
                .ratingAvg(new BigDecimal("4.70"))
                .reviewCount(29)
                .build();
        mug.addImage(ProductImage.builder()
                .imageUrl("https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80")
                .primary(true).displayOrder(0).build());
        mug.addVariant(ProductVariant.builder()
                .sku("MUG-CLAY").title("Speckled Clay").colorAttr("Sand").priceAdjustment(BigDecimal.ZERO).stockQuantity(40).active(true).build());
        mug.addVariant(ProductVariant.builder()
                .sku("MUG-BASALT").title("Matte Basalt").colorAttr("Black").priceAdjustment(BigDecimal.ZERO).stockQuantity(35).active(true).build());

        productRepository.saveAll(List.of(
                headphones, keyboard, monitor, smartWatch,
                tshirt, backpack, sweater,
                coffeeDripper, dutchOven, mug,
                yogaMat, bottle
        ));

        log.info("Successfully seeded 12 products, 28 variants, and 6 categories!");
    }
}
