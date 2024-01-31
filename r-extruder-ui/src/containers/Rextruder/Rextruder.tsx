import React from "react";
import "./Rextruder.scss";
import { Upload } from "@progress/kendo-react-upload";
import SingleSelect from "../../components/SingleSelect";

const Rextruder = () => {
  return (
    <main className={"r-extruder"}>
      <basf-header title={"R-extruder"}></basf-header>

      <div className="components-container">
        <div className="upload-componenet">
          <Upload
            batch={false}
            multiple={true}
            defaultFiles={[]}
            withCredentials={false}
            saveUrl={
              "https://demos.telerik.com/kendo-ui/service-v4/upload/save"
            }
            removeUrl={
              "https://demos.telerik.com/kendo-ui/service-v4/upload/remove"
            }
          />
        </div>
        <SingleSelect />
      </div>

      <basf-footer footer-title={"R-extruder"}></basf-footer>
    </main>
  );
};

export default Rextruder;
