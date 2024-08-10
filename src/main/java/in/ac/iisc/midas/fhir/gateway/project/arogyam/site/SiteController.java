package in.ac.iisc.midas.fhir.gateway.project.arogyam.site;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sites")
public class SiteController {
    private final SiteResponse response;

    public SiteController(ObjectMapper mapper) throws JsonProcessingException {
        response = mapper.readValue(getConfig(), SiteResponse.class);
    }

    @GetMapping
    public SiteResponse get() {
        return response;
    }

    private static String getConfig() {
        return """
                {
                    "type": "sitesConfig",
                    "environments": [
                      {
                        "type": "staging",
                        "site": {
                          "code": "staging",
                          "name": "Staging",
                          "fhirBaseUrl": "https://staging.arogyam-midas.iisc.ac.in/fhir/",
                          "authBaseUrl": "https://staging.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                          "authClientId": "arogyam-app",
                          "oauthClientId": "profile",
                          "mapSdkToken": "MAPBOX_SDK_TOKEN",
                          "openSrpAppId": "app",
                          "sentryDsn": ""
                        }
                      }
                    ],
                    "sites": [
                      {
                        "code": "1",
                        "name": "KLE Society's Institute of Dental Sciences, Bangalore",
                        "fhirBaseUrl": "https://site-1-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-1-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "2",
                        "name": "AIIMS, Delhi and NCI, Jhajjar",
                        "fhirBaseUrl": "https://site-2-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-2-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "3",
                        "name": "MSMF and MSMC, Narayana Hrudayalaya Health City, Bangalore",
                        "fhirBaseUrl": "https://site-3-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-3-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "4",
                        "name": "Public health facilities, Krishnagiri",
                        "fhirBaseUrl": "https://site-4-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-4-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "5",
                        "name": "Public health facilities, Thanjavur",
                        "fhirBaseUrl": "https://site-5-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-5-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "6",
                        "name": "MPMMCC, Varanasi",
                        "fhirBaseUrl": "https://site-6-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-6-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "7",
                        "name": "Cachar Cancer Hospital and Research Centre, Silchar",
                        "fhirBaseUrl": "https://site-7-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-7-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      },
                      {
                        "code": "8",
                        "name": "Dr.Bhubaneswar Borooah Cancer Institute, Guwahati",
                        "fhirBaseUrl": "https://site-8-production.arogyam-midas.iisc.ac.in/fhir/",
                        "authBaseUrl": "https://site-8-production.arogyam-midas.iisc.ac.in/auth/realms/arogyam/",
                        "authClientId": "arogyam-app",
                        "oauthClientId": "profile",
                        "mapSdkToken": "MAPBOX_SDK_TOKEN",
                        "openSrpAppId": "app",
                        "sentryDsn": ""
                      }
                    ]
                  }
                """;
    }
}
