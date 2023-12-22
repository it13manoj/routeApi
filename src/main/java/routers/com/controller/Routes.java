package routers.com.controller;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import localsolver.LSExpression;
import localsolver.LSModel;
import localsolver.LocalSolver;


@RestController
@RequestMapping("/api/v1")
public class Routes {
	 private static final double PI = 3.14159265359;
	 private final LocalSolver localSolver ;
	 
	 	private LSExpression R;
	    private LSExpression r;
	    private LSExpression h;

	    private LSExpression surface;
	    private LSExpression volume;
	    
	    private Routes(LocalSolver localsolver) {
	        this.localSolver = localsolver;
	    }
	    
 private void solve(int limit) {
	    	
	        // Declare the optimization model
	        LSModel model = localSolver.getModel();
	        LSExpression piConst = model.createConstant(PI);

	        // Numerical decisions
	        R = model.floatVar(0, 1);
	        r = model.floatVar(0, 1);
	        h = model.floatVar(0, 1);

	        // surface = PI*r^2 + PI*(R+r)*sqrt((R - r)^2 + h^2)
	        LSExpression s1 = model.prod(piConst, r, r);
	        LSExpression s2 = model.pow(model.sub(R, r), 2);
	        LSExpression s3 = model.pow(h, 2);
	        LSExpression s4 = model.sqrt(model.sum(s2, s3));
	        LSExpression s5 = model.sum(R, r);
	        LSExpression s6 = model.prod(piConst, s5, s4);
	        surface = model.sum(s1, s6);

	        // Surface must not exceed the surface of the plain disc
	        model.addConstraint(model.leq(surface, PI));

	        LSExpression v1 = model.pow(R, 2);
	        LSExpression v2 = model.prod(R, r);
	        LSExpression v3 = model.pow(r, 2);

	        // volume = PI*h/3*(R^2 + R*r + r^2)
	        volume = model.prod(piConst, model.div(h, 3), model.sum(v1, v2, v3));

	        // Maximize the volume
	        model.maximize(volume);

	        model.close();

	        // Parametrize the solver
	        localSolver.getParam().setTimeLimit(limit);

	        localSolver.solve();
	    }
 
 private void writeSolution(String fileName) throws IOException {
     try (PrintWriter output = new PrintWriter(fileName)) {
         output.println(surface.getDoubleValue() + " " + volume.getDoubleValue());
         output.println(R.getDoubleValue() + " " + r.getDoubleValue() + " " + h.getDoubleValue());
     }
 }

	
	@GetMapping("/find")
	public String findRoutes() {
		
		 String outputFile = "";
	        String strTimeLimit = "2";

	        try (LocalSolver localsolver = new LocalSolver()) {
	        	Routes model = new Routes(localsolver);
	            model.solve(Integer.parseInt(strTimeLimit));
	            if (outputFile != null) {
	                model.writeSolution(outputFile);
	            }
	        } catch (Exception ex) {
	            System.err.println(ex);
	            ex.printStackTrace();
	            System.exit(1);
	        }
		return null;
	}
}
