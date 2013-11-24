package mqfiletransferagent

object MqFileTransferAgent extends App {
	val usage = """
	  Usage: MqFileTransferAgent [--commandq commandQueueName] [--dataq dataQueueName] activeMqConnectionString
	"""
	  
	if (args.length == 0) {
	  println(usage)
	  sys.exit(-1)
	}
	
	def nextOption(map: Map[Symbol, Any], list: List[String]): Map[Symbol, Any] = {
	  list match {
	    case Nil => map
	    case "--commandq" :: commandQueueName :: tail =>
	      nextOption(map ++ Map('commandq -> commandQueueName), tail)
	    case "--dataq" :: dataQueueName :: tail =>
	      nextOption(map ++ Map('dataq -> dataQueueName), tail)
	    case "--transferSize" :: transferSize :: tail =>
	      nextOption(map ++ Map('transferSize -> transferSize.toInt), tail)
	    case activeMqConnectionString :: Nil =>
	      nextOption(map ++ Map('activeMqConnectionString -> activeMqConnectionString), list.tail)
	    case option :: tail =>
	      println("Unknown option " + option)
	      sys.exit(-1)
	  }
	}
	  
	val options = nextOption(Map(), args.toList)
}