package sammons

import java.util.UUID

import sammons.checkoutcartpricing.CartType._
import sammons.checkoutcartpricing._
import sammons.checkoutcartpricing.bundlerulesets.FlatRateSavingsRuleSet

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


  /* here's a flat rate deal that will stack with the custom one */

val flatRateBuyFourQualifyingCart = Map[CatalogItem, Int](margarin -> 4)
val flatRateBuyTwoQualifyingCart = Map[CatalogItem, Int](margarin -> 2)
val buyFourMargarinGetTwoUnitsOff = new FlatRateSavingsRuleSet(ruleSetId = "buyFour", subsetCart = flatRateBuyFourQualifyingCart, savings = 2)
val buyTwoMargarinGetOneUnitOff = new FlatRateSavingsRuleSet(ruleSetId = "buyTwo",subsetCart = flatRateBuyTwoQualifyingCart, savings = 1)
  /* creating a test cart */
val testCart: Cart = Map[CatalogItem, Int](
  margarin -> 4,
  bread -> 3
)

val checkoutCartPricingSystem = new CheckoutCartPricing(
catalogItems = catalog.values.toSeq,Seq(buyFourMargarinGetTwoUnitsOff, buyTwoMargarinGetOneUnitOff))

  /* calculate cart cost */
val checkoutCart = checkoutCartPricingSystem.calculateCheapestCheckoutCart(testCart)
println(s"original cost: ${checkoutCart.cart.price}, new cost: ${checkoutCart.total}, bundles: ${checkoutCart.applicableBundles.map(_.bundleRuleSetId.toString)}")
  /* should be 18, apply buyOneBreadAndMargarin Get the next margarin free twice, and buyFourMargarin get 2 off. */
}