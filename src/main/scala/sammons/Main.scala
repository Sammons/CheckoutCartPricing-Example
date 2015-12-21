package sammons

import java.util.UUID

import sammons.checkoutcartpricing._
import sammons.checkoutcartpricing.CheckoutCartPricingTypes.Cart

object Main extends App {

  /* set up the catalog */
val catalog = Map( /* map for convenience, who knows the guid ids in this demo? */
  "apples" -> CatalogItem(UUID.randomUUID().toString, "apples", 1.00),
  "oranges" -> CatalogItem(UUID.randomUUID().toString, "oranges", 2.00),
  "snickers" -> CatalogItem(UUID.randomUUID().toString, "snickers", 3.00),
  "bread" -> CatalogItem(UUID.randomUUID().toString, "bread", 4.00),
  "margarin" -> CatalogItem(UUID.randomUUID().toString, "margarin", 4.00)
)
  /* configuring a test deal, would probably want to create abstractions around this*/
val margarin = catalog.get("margarin").get
val bread = catalog.get("bread").get


val testIfCartContainsButterAndBread = (cart: Cart) => {
  cart.get(bread).isDefined && cart.get(margarin).isDefined
}

val qualifyingCartForBuyOneBreadThenNextMargarinFree = Map[CatalogItem, Int](margarin -> 1, bread -> 1)
val calculateBreadAndButterSavingsUntilNoLongerApplies: (Cart) => BigDecimal = (cart: Cart) => {
  if (testIfCartContainsButterAndBread(cart)) {
    /* note how we subtract the items here so they don't allow infinte savings */
    val cartToSubtract = Map[CatalogItem, Int](margarin -> 2, bread -> 1)
    margarin.value + calculateBreadAndButterSavingsUntilNoLongerApplies(CheckoutCartPricingOperations.subtractCarts(cart, cartToSubtract))
  } else 0
}

val buyOneBreadMargarinThenNextMargarinFree = new CustomBundle(
  qualificationMatcher = testIfCartContainsButterAndBread,
  savingsCalculator = calculateBreadAndButterSavingsUntilNoLongerApplies
)
  /* here's a flat rate deal that will stack with the custom one */

val flatRateQualifyingCart = Map[CatalogItem, Int](margarin -> 4)
val buyFourMargarinGetTwoUnitsOff = new FlatRateBundle(flatRateSavings = 2, qualifyingCart = flatRateQualifyingCart)

  /* creating a test cart */
val testCart: Cart = Map[CatalogItem, Int](
  margarin -> 4,
  bread -> 3
)

  /* initialize the API. Note how we use CheckoutCartPricingImpl - it is recommended to use Guice or something
   * similar, and bind CheckoutCartPricingImpl to the CheckoutCartPricing trait as the interface */
  CheckoutCartPricingImpl.initializeCheckoutCartPriceCalculationSystem(catalog.values.toSeq, Seq(buyOneBreadMargarinThenNextMargarinFree, buyFourMargarinGetTwoUnitsOff))

  /* calculate cart cost */
  println(s"cost ${CheckoutCartPricingImpl.calculateCheckoutCartPrice(testCart)}")
  /* should be 18, apply buyOneBreadAndMargarin Get the next margarin free twice, and buyFourMargarin get 2 off. */
}