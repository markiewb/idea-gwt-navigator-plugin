# idea-gwt-navigator-plugin
    
You can jump between GWT services using the "Related Symbol..." action.
Compatible to the IntelliJ IDEA Community Edition.

<br>
It allows you to
<ul>
<li>Jump from com.foo.<b>client</b>.(I)MyService             to com.foo.<b>client</b>.(I)MyService<b>Async</b> and com.foo.<b>server</b>.MyService<b>Impl</b></li>
<li>Jump from com.foo.<b>client</b>.(I)MyService<b>Async</b> to com.foo.<b>client</b>.(I)MyService and com.foo.<b>server</b>.MyService<b>Impl</b></li>
<li>Jump from com.foo.<b>server</b>.MyService<b>Impl</b>  to com.foo.<b>client</b>.(I)MyService and com.foo.<b>client</b>.(I)MyService<b>Async</b></li>
</ul>
If the caret is within/on a class, then you can jump from/to the related async/impl class. <br>
If the caret is within/on a method, then you can jump from/to the related async/impl method.<br>

<img src="https://raw.githubusercontent.com/markiewb/idea-gwt-navigator-plugin/master/doc/screenshot3.png"/>
<br>
Changelog:
<ul>
<li> 2.2: Fixed: Group results by prod and test source roots #4</li>
<li> 2.1: Fixed: Non-RemoteServlet-Implementation not found #3</li>
<li> 2.0: Detect more variants (by use of type information) #2</li>
<li> 1.2: Jump from/to IMyService(Async) to MyServiceImpl</li>
<li> 1.1: Jump from/to corresponding async/impl method</li>
<li> 1.0: Initial version - jump to files (regardless which method is currently active)</li>
</ul>
