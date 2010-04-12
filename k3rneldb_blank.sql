-- phpMyAdmin SQL Dump
-- version 3.2.1
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Apr 12, 2010 at 02:52 AM
-- Server version: 5.1.37
-- PHP Version: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";

--
-- Database: `k3rnel_mons`
--

-- --------------------------------------------------------

--
-- Table structure for table `kn_bag`
--

CREATE TABLE IF NOT EXISTS `kn_bag` (
  `member` int(11) NOT NULL,
  `item` int(11) NOT NULL,
  `quantity` int(11) NOT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `kn_bans`
--

CREATE TABLE IF NOT EXISTS `kn_bans` (
  `ip` varchar(48) DEFAULT NULL
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `kn_members`
--

CREATE TABLE IF NOT EXISTS `kn_members` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(12) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `dob` varchar(12) DEFAULT NULL,
  `email` varchar(32) DEFAULT NULL,
  `lastLoginTime` varchar(42) DEFAULT NULL,
  `lastLoginServer` varchar(32) DEFAULT NULL,
  `lastLoginIP` varchar(32) DEFAULT NULL,
  `sprite` int(11) DEFAULT NULL,
  `party` int(11) DEFAULT NULL,
  `money` int(11) DEFAULT NULL,
  `skHerb` int(11) DEFAULT NULL,
  `skCraft` int(11) DEFAULT NULL,
  `skFish` int(11) DEFAULT NULL,
  `skTrain` int(11) DEFAULT NULL,
  `skCoord` int(11) DEFAULT NULL,
  `skBreed` int(11) DEFAULT NULL,
  `x` int(11) DEFAULT NULL,
  `y` int(11) DEFAULT NULL,
  `mapX` int(11) DEFAULT NULL,
  `mapY` int(11) DEFAULT NULL,
  `badges` varchar(50) DEFAULT NULL,
  `healX` int(11) DEFAULT NULL,
  `healY` int(11) DEFAULT NULL,
  `healMapX` int(11) DEFAULT NULL,
  `healMapY` int(11) DEFAULT NULL,
  `isSurfing` varchar(5) DEFAULT NULL,
  `muted` varchar(12) DEFAULT NULL,
  `adminLevel` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=10 ;

-- --------------------------------------------------------

--
-- Table structure for table `kn_party`
--

CREATE TABLE IF NOT EXISTS `kn_party` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `member` int(11) DEFAULT NULL,
  `monster0` int(11) DEFAULT NULL,
  `monster1` int(11) DEFAULT NULL,
  `monster2` int(11) DEFAULT NULL,
  `monster3` int(11) DEFAULT NULL,
  `monster4` int(11) DEFAULT NULL,
  `monster5` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=9 ;

-- --------------------------------------------------------

--
-- Table structure for table `kn_monster`
--

CREATE TABLE IF NOT EXISTS `kn_monster` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(24) DEFAULT NULL,
  `speciesName` varchar(32) DEFAULT NULL,
  `exp` varchar(32) DEFAULT NULL,
  `baseExp` int(11) DEFAULT NULL,
  `expType` varchar(16) DEFAULT NULL,
  `isFainted` varchar(5) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  `happiness` int(11) DEFAULT NULL,
  `gender` int(11) DEFAULT NULL,
  `nature` varchar(24) DEFAULT NULL,
  `abilityName` varchar(24) DEFAULT NULL,
  `itemName` varchar(28) DEFAULT NULL,
  `isShiny` varchar(5) DEFAULT NULL,
  `originalTrainerName` varchar(12) DEFAULT NULL,
  `currentTrainerName` varchar(12) DEFAULT NULL,
  `contestStats` varchar(255) DEFAULT NULL,
  `move0` varchar(32) DEFAULT NULL,
  `move1` varchar(32) DEFAULT NULL,
  `move2` varchar(32) DEFAULT NULL,
  `move3` varchar(32) DEFAULT NULL,
  `hp` int(11) DEFAULT NULL,
  `atk` int(11) DEFAULT NULL,
  `def` int(11) DEFAULT NULL,
  `speed` int(11) DEFAULT NULL,
  `spATK` int(11) DEFAULT NULL,
  `spDEF` int(11) DEFAULT NULL,
  `evHP` int(11) DEFAULT NULL,
  `evATK` int(11) DEFAULT NULL,
  `evDEF` int(11) DEFAULT NULL,
  `evSPD` int(11) DEFAULT NULL,
  `evSPATK` int(11) DEFAULT NULL,
  `evSPDEF` int(11) DEFAULT NULL,
  `ivHP` int(11) DEFAULT NULL,
  `ivATK` int(11) DEFAULT NULL,
  `ivDEF` int(11) DEFAULT NULL,
  `ivSPD` int(11) DEFAULT NULL,
  `ivSPATK` int(11) DEFAULT NULL,
  `ivSPDEF` int(11) DEFAULT NULL,
  `pp0` int(11) DEFAULT NULL,
  `pp1` int(11) DEFAULT NULL,
  `pp2` int(11) DEFAULT NULL,
  `pp3` int(11) DEFAULT NULL,
  `maxpp0` int(11) DEFAULT NULL,
  `maxpp1` int(11) DEFAULT NULL,
  `maxpp2` int(11) DEFAULT NULL,
  `maxpp3` int(11) DEFAULT NULL,
  `ppUp0` int(11) DEFAULT NULL,
  `ppUp1` int(11) DEFAULT NULL,
  `ppUp2` int(11) DEFAULT NULL,
  `ppUp3` int(11) DEFAULT NULL,
  `date` varchar(28) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=51 ;

-- --------------------------------------------------------

--
-- Table structure for table `kn_track_cards`
--

CREATE TABLE IF NOT EXISTS `kn_track_cards` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `trainerId` int(11) NOT NULL,
  `ip` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  `referer` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `points` int(11) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Track people who click Trainer Cards' AUTO_INCREMENT=12 ;

-- --------------------------------------------------------

--
-- Table structure for table `monsterdex_evs`
--

CREATE TABLE IF NOT EXISTS `monsterdex_evs` (
  `eid` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(11) NOT NULL,
  `hp` int(11) NOT NULL,
  `attack` int(11) NOT NULL,
  `defense` int(11) NOT NULL,
  `spatk` int(11) NOT NULL,
  `spdef` int(11) NOT NULL,
  `speed` int(11) NOT NULL,
  PRIMARY KEY (`eid`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=500 ;

-- --------------------------------------------------------

--
-- Table structure for table `monsterdex_monster`
--

CREATE TABLE IF NOT EXISTS `monsterdex_monster` (
  `pid` int(11) NOT NULL AUTO_INCREMENT,
  `num` int(11) NOT NULL,
  `name` varchar(25) NOT NULL,
  `kind` varchar(20) NOT NULL,
  `dexdata` varchar(300) NOT NULL,
  `type1` varchar(25) NOT NULL,
  `type2` varchar(25) NOT NULL,
  `growthrate` varchar(30) NOT NULL,
  `stepstohatch` int(11) NOT NULL,
  `color` varchar(15) NOT NULL,
  `habitat` varchar(30) NOT NULL,
  `height` double NOT NULL,
  `weight` double NOT NULL,
  `femmale` int(11) NOT NULL,
  PRIMARY KEY (`pid`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=500 ;

-- --------------------------------------------------------

--
-- Table structure for table `monsterdex_stats`
--

CREATE TABLE IF NOT EXISTS `monsterdex_stats` (
  `sid` int(11) NOT NULL AUTO_INCREMENT,
  `pid` int(11) NOT NULL,
  `hp` int(11) NOT NULL,
  `attack` int(11) NOT NULL,
  `defense` int(11) NOT NULL,
  `spatk` int(11) NOT NULL,
  `spdef` int(11) NOT NULL,
  `speed` int(11) NOT NULL,
  PRIMARY KEY (`sid`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1 AUTO_INCREMENT=500 ;

