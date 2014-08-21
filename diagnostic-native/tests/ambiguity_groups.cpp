#include <boost/test/unit_test.hpp>
#include <libdiag/structs/ambiguity_groups.h>
#include <libdiag/structs/count_spectrum.h>

#include <fstream>
using namespace diagnostic;
using namespace diagnostic::structs;
using namespace std;

BOOST_AUTO_TEST_SUITE(AMBIGUITY_GROUPS)

BOOST_AUTO_TEST_CASE(AG1) {
    t_count_spectrum spectrum;
    t_ambiguity_groups ag(spectrum);
    stringstream ss("2 2\
           1 1 1\
           1 1 1");


    ss >> spectrum;
    ag = t_ambiguity_groups(spectrum);

    BOOST_CHECK(!ag.filter().components.is_filtered(1));
    BOOST_CHECK(ag.filter().components.is_filtered(2));
    BOOST_CHECK(ag.group(1)->count(2));
    BOOST_CHECK(ag.group(1)->size() == 1);

    ss.str("3 2\
           1 1 0 1\
           1 1 1 1");
    ss.clear();

    spectrum = t_count_spectrum();
    ss >> spectrum;
    ag = t_ambiguity_groups(spectrum);


    BOOST_CHECK(!ag.filter().components.is_filtered(1));
    BOOST_CHECK(ag.filter().components.is_filtered(2));
    BOOST_CHECK(!ag.filter().components.is_filtered(3));
    BOOST_CHECK(ag.group(1)->count(2));
    BOOST_CHECK(ag.group(1)->size() == 1);

    ss.str("10 3\
           0 1 0 0 1 0 1 1 1 1 1\
           1 1 1 1 1 1 0 0 1 0 1\
           1 1 1 0 1 0 1 0 1 1 1");
    ss.clear();

    spectrum = t_count_spectrum();
    ss >> spectrum;
    ag = t_ambiguity_groups(spectrum);

    BOOST_CHECK(!ag.filter().components.is_filtered(1));
    BOOST_CHECK(!ag.filter().components.is_filtered(2));
    BOOST_CHECK(ag.filter().components.is_filtered(3));
    BOOST_CHECK(!ag.filter().components.is_filtered(4));
    BOOST_CHECK(ag.filter().components.is_filtered(5));
    BOOST_CHECK(ag.filter().components.is_filtered(6));
    BOOST_CHECK(!ag.filter().components.is_filtered(7));
    BOOST_CHECK(!ag.filter().components.is_filtered(8));
    BOOST_CHECK(ag.filter().components.is_filtered(9));
    BOOST_CHECK(ag.filter().components.is_filtered(10));

    BOOST_CHECK(ag.group(1)->count(3));
    BOOST_CHECK(ag.group(1)->size() == 1);

    BOOST_CHECK(ag.group(2)->count(5));
    BOOST_CHECK(ag.group(2)->count(9));
    BOOST_CHECK(ag.group(2)->size() == 2);

    BOOST_CHECK(ag.group(4)->count(6));
    BOOST_CHECK(ag.group(4)->size() == 1);

    BOOST_CHECK(ag.group(7)->count(10));
    BOOST_CHECK(ag.group(7)->size() == 1);
}

BOOST_AUTO_TEST_SUITE_END()
