package connectors

/**
 * Created by Costantinos on 26/6/2015.
 */
trait Connector {
     def parse(input:String):ConnectorObject
}
