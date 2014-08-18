/*
 * This material is distributed under the GNU General Public License
 * Version 2. You may review the terms of this license at
 * http://www.gnu.org/licenses/gpl-2.0.html
 * 
 * Copyright (c) 2014, Purdue University
 * Copyright (c) 2014, Oracle and/or its affiliates
 *
 * All rights reserved.
 */
package com.oracle.truffle.r.test.testrgen;

import org.junit.*;

import com.oracle.truffle.r.test.*;

public class TestrGenBuiltincolMeans extends TestBase {

    @Test
    @Ignore
    public void testcolMeans1() {
        assertEval("argv <- list(structure(1:5, .Dim = c(5L, 1L), .Dimnames = list(c(\'1\', \'2\', \'3\', \'4\', \'5\'), \'a\')), 5, 1, FALSE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans2() {
        assertEval("argv <- list(structure(c(135L, 49L, 32L, NA, 64L, 40L, 77L, 97L, 97L, 85L, NA, 10L, 27L, NA, 7L, 48L, 35L, 61L, 79L, 63L, 16L, NA, NA, 80L, 108L, 20L, 52L, 82L, 50L, 64L, 59L), .Dim = c(31L, 1L)), 31, 1, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans3() {
        assertEval("argv <- list(structure(c(2, 1, 0, 1, 0, NA, NA, NA, 0), .Dim = c(3L, 3L)), 3, 3, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans4() {
        assertEval("argv <- list(structure(numeric(0), .Dim = c(0L, 0L)), 0, 0, FALSE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans5() {
        assertEval("argv <- list(structure(c(3, 3, NA, 3, 3, 3, 3, 3, 4, 3, NA, NA, 2, 3, 4, 5), .Dim = c(8L, 2L), .Dimnames = list(NULL, c(\'x1\', \'x2\'))), 8, 2, FALSE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans6() {
        assertEval("argv <- list(structure(c(2.72365184362824, -0.796449881281511, 0.796148249055565, 2.02271745300814, 1.2802770548002, 1.337056204255, 2.6107240701292, 2.10828628469836, 1.43875587381801, 0.595316954266145, 3.49563715531476, 1.48558049063486, 1.41226464164167, 2.44508400241911, 2.57984360481665, -1.20772288776711, 2.43688378444644, 0.533076425061003, 1.3034273968671, 3.70362282204711, 0.608593870458723, 0.953140512120224, 0.386257122548769, 3.75339780206139, 3.42482460204159, 0.619548124388308, 3.14887764228466, 0.751148471471006, 1.87137783412956, 1.62046400462005, 0.94345287218966, 1.64506447351138, 1.30606946576658, 2.45886447346843, 2.03431588373773, -0.338520676288604, 2.03487908340323, 1.29252851374035, 1.2267540180234, 2.05222753374982, 2.83569182599169, 3.48408642621763, 2.39258208808217, 1.92637187747015, 1.9464722888473, 0.936987622444041, -0.457825141151114, 0.770998483524033, 1.80682914026242, 0.916590155958594, 2.78096073652237, 1.55750387883765, 3.3880545417157, 3.65182127019008, 1.46303726963845, 2.58757002961928, 2.44326477189276, 0.94225036142597, 1.29219317072567, 2.94893881867, 1.24384829814308, 1.84885702049451, 1.32523566412607, 1.28973308890195, 0.395997063065922, 1.22892077218378, 1.7220093913143, 0.805646559888977, 1.01315020534677, 1.31726023805076, 1.72638291442835, 1.7933922500199, 1.62417301864782, 2.84632954278294, 1.06390349068226, 0.393286798351562, 2.85644316208756, 1.17640470313741, 0.528983054741685, 1.96126284937392, 0.917057889286139, 2.45214192885654, 2.46901056075969, 0.964752028993787, 1.15564656732576, 1.8050377493702, 3.92150679994132, 1.89242778597682, 1.79539660063946, 3.11975967552643, 3.60233448863085, 1.28811938153997, 2.49044833125605, 2.82723855540917, 0.488353198794268), .Dim = c(95L, 1L)), 95, 1, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans7() {
        assertEval("argv <- list(structure(FALSE, .Dim = c(1L, 1L)), 1, 1, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    @Ignore
    public void testcolMeans8() {
        assertEval("argv <- list(structure(c(234.505586749024, 30.477338238484, 110.520869124744, 10.8182256360112, 147.313169560589, 97.6285379622695, 176.128082322087, 47.2454421006426, 1.90674769458181e-15, 30.477338238484, 416.975499504725, 31.7861370189749, 190.703952476833, 120.117506711705, 442.506661969244, 239.889830502368, 22.2127533877541, 8.96469890623342e-16, 110.520869124744, 31.7861370189749, 1515.40698347813, 93.4417828515041, 178.042033105564, 210.514489336906, 228.304319294085, 24.2402628282532, 9.78296809359315e-16, 10.8182256360112, 190.703952476833, 93.4417828515041, 1736.17011782569, 171.990208955525, 616.163154757563, 314.295577560061, 190.513839846008, 7.68882264110221e-15, 147.313169560589, 120.117506711705, 178.042033105564, 171.990208955525, 4391.22673539453, 270.845832643245, 258.906125067947, 151.459157745218, 6.11264043711995e-15, 97.6285379622695, 442.506661969244, 210.514489336906, 616.163154757563, 270.845832643245, 3843.51687278644, 444.735756817902, 537.305365376654, 2.16847535162432e-14, 176.128082322087, 239.889830502368, 228.304319294085, 314.295577560062, 258.906125067947, 444.735756817902, 5767.34674134268, 307.533224133396, 1.24115309340219e-14, 47.2454421006426, 22.2127533877541, 24.2402628282532, 190.513839846008, 151.459157745218, 537.305365376655, 307.533224133396, 264.760049944031, 1.06852765558369e-14, 1.90674769458181e-15, 8.96469890623341e-16, 9.78296809359316e-16, 7.6888226411022e-15, 6.11264043711995e-15, 2.16847535162432e-14, 1.24115309340219e-14, 1.06852765558369e-14, 4.31240042063952e-31), .Dim = c(9L, 9L)), 9, 9, FALSE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    public void testcolMeans9() {
        assertEval("argv <- list(structure(c(NA, 17.4716236802524, 0.424429400003, -2.45474630431729, -8.6855922903657, -11.7956139807344, -8.08147081196715, -13.3123167980156, -1.24650334752019, 21.281002075072, -5.32311940332657, 0.621869751489083, -19.2022951076469, -0.543162784063959, NA, NA, 15.344649382745, -9.74060313555005, 0.149375174081257, -5.85062482591874, -6.90563567110309, -9.96064651628744, 5.6326723568001, -8.78481137542338, -6.01565736147178, -15.543162784064, 2.34681552556734, -13.2465033475202, -3.82901961529671, 1.5226506664314, NA, -5.9777558474085, 22.7534966524798, 15.5010454558094, 4.13857256877024, -11.6855922903657, 11.6768805966734, -7.38893285382193, 10.8527157375375, -11.3889328538219, 14.1493751740813, -0.388932853821931, 13.0835617235859, -1.98225172690947, 5.96273742790618, -1.50975714950164, -1.38893285382193, 9.90772658272184, 7.3144077096343, -12.9822517269095, 2.02855087840155, -4.7956139807344, 3.14937517408126, -10.3231194033266, -2.25730595283121, 2.56685890630474, 4.27019946976097, 5.14937517408126, 0.0285508784015471, 5.85271573753749, 6.73189144185778, -6.38893285382193, 0.0285508784015471, -3.14728426246251, 15.1493751740813, 13.7869022870421, -7.27891116345324, 9.61106714617807, 4.84191313222647, -3.98225172690947, -6.38893285382193, 13.0285508784015, 5.13857256877024, -8.50975714950164, -0.619778839870337, -3.97144912159845, 23.1493751740813, -2.80641658604541, -1.03726257209382, 2.25939686444995, 4.25939686444995, -4.38893285382193, 6.38022116012966, -4.74060313555005, 2.02855087840155, -15.7956139807344, 8.21518862457662, -12.0264599667828, -2.1364816571515, 5.8635183428485, -14.729800530239, 4.80850749766416, -11.7848113754234, 9.45683721593604, -15.2573059528312, 5.28100207507198, 12.8635183428485, 6.50104545580937, 1.55605630099372, -7.44394369900628, 9.9735400332172, -11.2681085581422, 7.44603461062503, -8.14728426246251, -1.72980053023903, -3.90563567110309, 4.56685890630474, -5.37813024851092, -1.25730595283121, 10.7426940471688, NA, NA, 6.24343998511081, -21.9164382764141, -6.1364816571515, -15.8398222206077, -4.12567905184048, -7.94984391097642, -6.4773493335686, -5.65318447443266), .Dim = c(120L, 1L)), 120, 1, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }

    @Test
    @Ignore
    public void testcolMeans10() {
        assertEval("argv <- list(structure(c(NA, 30.6929824561403, 25.6929824561403, 18.6929824561403, 6.69298245614035, -6.30701754385965, -13.3070175438597, -24.3070175438597, -21.3070175438597, 3.69298245614035, -2.30701754385965, -1.30701754385965, -20.3070175438597, -17.3070175438597, NA, NA, 12.6929824561403, 0.692982456140349, 0.692982456140349, -5.30701754385965, -11.3070175438597, -19.3070175438597, -10.3070175438597, -17.3070175438597, -20.3070175438597, -32.3070175438597, -24.3070175438597, -33.3070175438597, -31.3070175438597, -24.3070175438597, NA, -24.3070175438597, 2.69298245614035, 17.6929824561403, 18.6929824561403, 3.69298245614035, 14.6929824561403, 4.69298245614035, 14.6929824561403, 0.692982456140349, 14.6929824561403, 11.6929824561403, 22.6929824561403, 16.6929824561403, 19.6929824561403, 14.6929824561403, 10.6929824561403, 18.6929824561403, 22.6929824561403, 5.69298245614035, 6.69298245614035, 0.692982456140349, 3.69298245614035, -7.30701754385965, -8.30701754385965, -4.30701754385965, 0.692982456140349, 5.69298245614035, 4.69298245614035, 9.69298245614035, 14.6929824561403, 5.69298245614035, 4.69298245614035, 0.692982456140349, 15.6929824561403, 26.6929824561403, 14.6929824561403, 21.6929824561403, 22.6929824561403, 14.6929824561403, 5.69298245614035, 17.6929824561403, 19.6929824561403, 7.69298245614035, 5.69298245614035, 0.692982456140349, 23.6929824561403, 16.6929824561403, 12.6929824561403, 12.6929824561403, 14.6929824561403, 7.69298245614035, 12.6929824561403, 5.69298245614035, 6.69298245614035, -10.3070175438597, -0.307017543859651, -12.3070175438597, -12.3070175438597, -4.30701754385965, -18.3070175438597, -10.3070175438597, -20.3070175438597, -7.30701754385965, -21.3070175438597, -12.3070175438597, 2.69298245614035, 8.69298245614035, 8.69298245614035, -0.307017543859651, 9.69298245614035, -3.30701754385965, 4.69298245614035, -4.30701754385965, -5.30701754385965, -8.30701754385965, -2.30701754385965, -7.30701754385965, -7.30701754385965, 4.69298245614035, NA, NA, 11.6929824561403, -12.3070175438597, -16.3070175438597, -29.3070175438597, -28.3070175438597, -31.3070175438597, -32.3070175438597, -32.3070175438597), .Dim = c(120L, 1L), \'`scaled:center`\' = 56.3070175438597, .Dimnames = list(NULL, \'Series 1\'), .Tsp = c(1, 120, 1), class = \'ts\'), 120, 1, TRUE); .Internal(colMeans(argv[[1]], argv[[2]], argv[[3]], argv[[4]]))");
    }
}